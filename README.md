# 🚀 Social App — Spring Boot Microservices on OpenShift

A production-ready multi-module Maven project with two Spring Boot microservices deployed to an OpenShift/Kubernetes cluster.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   OpenShift Cluster  (social-app ns)         │
│                                                               │
│   ┌──────────────────┐         ┌──────────────────┐          │
│   │   user-service   │◄────────│   post-service   │          │
│   │   :8080          │ WebClient│   :8080          │          │
│   │   (Spring Boot)  │         │   (Spring Boot)  │          │
│   └────────┬─────────┘         └────────┬─────────┘          │
│            │                            │                     │
│   ┌────────▼─────────┐         ┌────────▼─────────┐          │
│   │   user-db        │         │   post-db         │         │
│   │   PostgreSQL 15  │         │   PostgreSQL 15   │         │
│   │   (PVC: 1Gi)     │         │   (PVC: 1Gi)      │         │
│   └──────────────────┘         └──────────────────┘          │
│                                                               │
│   ┌─────────────────────┐  ┌─────────────────────┐           │
│   │  Route (HTTPS)      │  │  Route (HTTPS)      │           │
│   │  user-service-route │  │  post-service-route │           │
│   └─────────────────────┘  └─────────────────────┘           │
└─────────────────────────────────────────────────────────────┘
                         ▲                  ▲
                    External Traffic (TLS Edge Termination)
```

---

## 📁 Project Structure

```
social-app/
├── pom.xml                          ← Parent POM (dependency management)
├── deploy.sh                        ← One-command build + deploy script
│
├── user-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/socialapp/userservice/
│       │   ├── UserServiceApplication.java
│       │   ├── controller/UserController.java
│       │   ├── service/UserService.java
│       │   ├── repository/UserRepository.java
│       │   ├── model/User.java
│       │   ├── dto/UserDto.java
│       │   └── config/GlobalExceptionHandler.java
│       └── resources/application.properties
│
├── post-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/socialapp/postservice/
│       │   ├── PostServiceApplication.java
│       │   ├── controller/PostController.java
│       │   ├── service/PostService.java
│       │   ├── service/UserServiceClient.java    ← WebClient inter-service call
│       │   ├── repository/PostRepository.java
│       │   ├── model/Post.java
│       │   ├── dto/PostDto.java
│       │   └── config/
│       │       ├── WebClientConfig.java
│       │       └── GlobalExceptionHandler.java
│       └── resources/application.properties
│
└── k8s/
    ├── namespace.yaml
    ├── user-service/
    │   ├── secret-and-config.yaml   ← Secret + ConfigMap
    │   ├── postgres.yaml            ← PVC + DB Deployment + DB Service
    │   └── deployment.yaml          ← App Deployment + Service + Route
    └── post-service/
        ├── postgres.yaml
        └── deployment.yaml
```

---

## 🔌 API Endpoints

### User Service

| Method | Endpoint                      | Description           |
|--------|-------------------------------|-----------------------|
| POST   | `/users`                      | Register a new user   |
| GET    | `/users`                      | List all users        |
| GET    | `/users/{id}`                 | Get user by ID        |
| GET    | `/users/username/{username}`  | Get user by username  |
| PATCH  | `/users/{id}`                 | Update user profile   |
| DELETE | `/users/{id}`                 | Delete user           |
| GET    | `/actuator/health`            | Health check          |

### Post Service

| Method | Endpoint                      | Description                      |
|--------|-------------------------------|----------------------------------|
| POST   | `/posts`                      | Create a post (validates userId) |
| GET    | `/posts`                      | List all posts (newest first)    |
| GET    | `/posts/{id}`                 | Get post by ID                   |
| GET    | `/posts/user/{userId}`        | All posts by a user              |
| PATCH  | `/posts/{id}`                 | Update post content              |
| DELETE | `/posts/{id}`                 | Delete post                      |
| GET    | `/actuator/health`            | Health check                     |

---

## ⚙️ Prerequisites

- Java 17+
- Maven 3.9+
- Docker (logged in to Docker Hub: `docker login`)
- `kubectl` configured for your OpenShift cluster  
  **or** `oc` CLI logged in: `oc login <cluster-url>`

---

## 🚀 Quick Deploy

```bash
# 1. Clone / navigate to the project root
cd social-app

# 2. Make deploy script executable
chmod +x deploy.sh

# 3. Run the full deploy (builds → pushes → applies to cluster)
./deploy.sh your-dockerhub-username 1.0.0
```

---

## 🔧 Manual Steps (if not using deploy.sh)

### Build

```bash
# From project root
mvn clean package -DskipTests
```

### Build & Push Docker Images

```bash
# User Service (run from project root — Dockerfile needs parent context)
docker build -f user-service/Dockerfile -t your-dockerhub-username/user-service:1.0.0 .
docker push your-dockerhub-username/user-service:1.0.0

# Post Service
docker build -f post-service/Dockerfile -t your-dockerhub-username/post-service:1.0.0 .
docker push your-dockerhub-username/post-service:1.0.0
```

### Update Image References

Edit `k8s/user-service/deployment.yaml` and `k8s/post-service/deployment.yaml`:
```yaml
image: your-dockerhub-username/user-service:1.0.0   # ← replace
image: your-dockerhub-username/post-service:1.0.0  # ← replace
```

### Apply Manifests

```bash
# Namespace
kubectl apply -f k8s/namespace.yaml

# User Service (DB first)
kubectl apply -f k8s/user-service/secret-and-config.yaml
kubectl apply -f k8s/user-service/postgres.yaml
kubectl rollout status deployment/user-db -n social-app
kubectl apply -f k8s/user-service/deployment.yaml
kubectl rollout status deployment/user-service -n social-app

# Post Service (DB first, app second)
kubectl apply -f k8s/post-service/postgres.yaml
kubectl rollout status deployment/post-db -n social-app
kubectl apply -f k8s/post-service/deployment.yaml
kubectl rollout status deployment/post-service -n social-app
```

### Check Routes (OpenShift)

```bash
oc get routes -n social-app
```

---

## 🧪 Test the APIs

```bash
# Set your route URLs (from oc get routes)
USER_URL="https://user-service-route-social-app.<cluster-domain>"
POST_URL="https://post-service-route-social-app.<cluster-domain>"

# 1. Create a user
curl -s -X POST "${USER_URL}/users" \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","displayName":"Alice","bio":"Hello!"}' | jq

# 2. Get user (use the id from step 1)
curl -s "${USER_URL}/users/1" | jq

# 3. Create a post (userId must exist)
curl -s -X POST "${POST_URL}/posts" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"content":"Hello, social world! 👋"}' | jq

# 4. Get all posts
curl -s "${POST_URL}/posts" | jq

# 5. Get posts by user
curl -s "${POST_URL}/posts/user/1" | jq
```

---

## 🔒 Production Checklist

- [ ] Replace base64 passwords in `secret-and-config.yaml` with strong credentials
- [ ] Use a proper Kubernetes Secret manager (Vault, Sealed Secrets, or External Secrets Operator)
- [ ] Set `DDL_AUTO=validate` (not `update`) after initial schema creation
- [ ] Add resource quotas and network policies
- [ ] Enable HPA (Horizontal Pod Autoscaler) for both services
- [ ] Consider a PostgreSQL Operator (CrunchyData, CloudNative-PG) for HA databases
- [ ] Set up centralized logging (ELK / OpenShift Logging)
- [ ] Add distributed tracing (OpenTelemetry / Jaeger)

---

## 🌐 Inter-Service Communication

Post Service calls User Service using **WebClient** over the Kubernetes internal DNS:

```
http://user-service.social-app.svc.cluster.local:8080
```

This is injected via the `USER_SERVICE_URL` environment variable in the Post Service Deployment. No service mesh required for this simple setup.

---

## 📦 Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.2.3 |
| Java Version | 17 (LTS) |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| HTTP Client | Spring WebFlux WebClient |
| Container | Docker (eclipse-temurin:17-jre-alpine) |
| Registry | Docker Hub |
| Orchestration | OpenShift 4.x / Kubernetes |
| Build | Maven 3.9 (multi-module) |
