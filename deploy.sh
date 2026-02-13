#!/bin/bash
# ─────────────────────────────────────────────────────────────────────
# deploy.sh — Build, push, and deploy social-app to OpenShift/K8s
# Usage: ./deploy.sh <dockerhub-username> [image-tag]
# ─────────────────────────────────────────────────────────────────────
set -euo pipefail

DOCKER_USER="${1:?Usage: ./deploy.sh <dockerhub-username> [tag]}"
TAG="${2:-1.0.0}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

USER_IMAGE="${DOCKER_USER}/user-service:${TAG}"
POST_IMAGE="${DOCKER_USER}/post-service:${TAG}"

echo "══════════════════════════════════════════════════════"
echo " Social App Deployment"
echo " Docker Hub User : ${DOCKER_USER}"
echo " Image Tag       : ${TAG}"
echo "══════════════════════════════════════════════════════"

# ─── Step 1: Maven build ─────────────────────────────────────────────
echo ""
echo "▶ [1/5] Building Maven modules..."
cd "${SCRIPT_DIR}"
mvn clean package -DskipTests -q
echo "  ✔ Build complete"

# ─── Step 2: Build Docker images ─────────────────────────────────────
echo ""
echo "▶ [2/5] Building Docker images..."

# Build user-service image (Dockerfile is inside user-service/ but needs parent context)
docker build \
  -f "${SCRIPT_DIR}/user-service/Dockerfile" \
  -t "${USER_IMAGE}" \
  "${SCRIPT_DIR}"
echo "  ✔ Built ${USER_IMAGE}"

# Build post-service image
docker build \
  -f "${SCRIPT_DIR}/post-service/Dockerfile" \
  -t "${POST_IMAGE}" \
  "${SCRIPT_DIR}"
echo "  ✔ Built ${POST_IMAGE}"

# ─── Step 3: Push to Docker Hub ──────────────────────────────────────
echo ""
echo "▶ [3/5] Pushing images to Docker Hub..."
docker push "${USER_IMAGE}"
echo "  ✔ Pushed ${USER_IMAGE}"
docker push "${POST_IMAGE}"
echo "  ✔ Pushed ${POST_IMAGE}"

# ─── Step 4: Patch image references in manifests ─────────────────────
echo ""
echo "▶ [4/5] Patching image references in K8s manifests..."
sed -i "s|your-dockerhub-username/user-service:1.0.0|${USER_IMAGE}|g" \
  "${SCRIPT_DIR}/k8s/user-service/deployment.yaml"
sed -i "s|your-dockerhub-username/post-service:1.0.0|${POST_IMAGE}|g" \
  "${SCRIPT_DIR}/k8s/post-service/deployment.yaml"
echo "  ✔ Manifests updated"

# ─── Step 5: Apply to cluster ────────────────────────────────────────
echo ""
echo "▶ [5/5] Applying Kubernetes / OpenShift manifests..."

kubectl apply -f "${SCRIPT_DIR}/k8s/namespace.yaml"

# User Service: databases first, then app
kubectl apply -f "${SCRIPT_DIR}/k8s/user-service/secret-and-config.yaml"
kubectl apply -f "${SCRIPT_DIR}/k8s/user-service/postgres.yaml"
echo "  Waiting for User DB to be ready..."
kubectl rollout status deployment/user-db -n social-app --timeout=120s
kubectl apply -f "${SCRIPT_DIR}/k8s/user-service/deployment.yaml"
echo "  Waiting for User Service to be ready..."
kubectl rollout status deployment/user-service -n social-app --timeout=180s

# Post Service: databases first, then app
kubectl apply -f "${SCRIPT_DIR}/k8s/post-service/postgres.yaml"
echo "  Waiting for Post DB to be ready..."
kubectl rollout status deployment/post-db -n social-app --timeout=120s
kubectl apply -f "${SCRIPT_DIR}/k8s/post-service/deployment.yaml"
echo "  Waiting for Post Service to be ready..."
kubectl rollout status deployment/post-service -n social-app --timeout=180s

# ─── Print routes ────────────────────────────────────────────────────
echo ""
echo "══════════════════════════════════════════════════════"
echo " ✅ Deployment complete!"
echo ""
echo " OpenShift Routes:"
oc get routes -n social-app 2>/dev/null || \
  echo "  (oc not found — use 'kubectl get svc -n social-app' to check services)"
echo "══════════════════════════════════════════════════════"
