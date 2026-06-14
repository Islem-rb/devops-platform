# DevOps Observability Platform

A production-grade microservices platform built to demonstrate end-to-end DevOps practices.

## Architecture

3 Spring Boot microservices deployed on Kubernetes with full observability stack.
┌─────────────────────────────────────────────────┐

│              Kubernetes Cluster (kind)           │

│                                                 │

│  ┌─────────────┐  ┌───────────────┐  ┌───────────────┐  │

│  │ user-service│  │product-service│  │order-service  │  │

│  │   :8081     │  │    :8082      │  │    :8083      │  │

│  └─────────────┘  └───────────────┘  └───────────────┘  │

│                                                 │

│  ┌─────────────┐  ┌───────────────┐  ┌───────────────┐  │

│  │ Prometheus  │  │    Grafana    │  │     Loki      │  │

│  └─────────────┘  └───────────────┘  └───────────────┘  │

│                                                 │

│  ┌─────────────────────────────────────────┐   │

│  │              Jenkins CI/CD              │   │

│  └─────────────────────────────────────────┘   │

└─────────────────────────────────────────────────┘

## Tech Stack

- **Runtime**: Kubernetes (kind) — 1 control-plane + 2 workers
- **Services**: Spring Boot 3.5, Java 21, Maven
- **Observability**: Prometheus, Grafana, Loki, Promtail
- **CI/CD**: Jenkins with GitHub webhooks
- **Containerization**: Docker

## Services

| Service | Port | Endpoints |
|---|---|---|
| user-service | 8081 | GET /users, GET /users/{id} |
| product-service | 8082 | GET /products, GET /products/{id} |
| order-service | 8083 | GET /orders, POST /orders |

## Setup

```bash
# Create kind cluster
kind create cluster --name devops-platform --config kind-cluster.yaml

# Deploy services
kubectl apply -f k8s/

# Check pods
kubectl get pods -n devops-platform
```

## Status

- [x] Microservices deployed on Kubernetes
- [ ] Prometheus + Grafana observability
- [ ] Loki log aggregation
- [ ] Jenkins CI/CD pipeline
