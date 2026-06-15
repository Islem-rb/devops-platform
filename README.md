# DevOps Observability Platform

A complete DevOps observability platform running on Kubernetes (kind) with three Spring Boot microservices, a full observability stack (Prometheus, Grafana, Loki), and a Jenkins CI/CD pipeline that automatically builds, tests, pushes Docker images, loads them into kind, and performs rolling updates.

---

## 📦 Project Overview

- **Three microservices** (User, Product, Order) built with Spring Boot 3.5.0 and Java 21.
- **Kubernetes cluster** – kind (Kubernetes in Docker) with 1 control plane and 2 workers.
- **Observability Stack** – Prometheus for metrics, Grafana for dashboards, Loki + Promtail for log aggregation.
- **CI/CD Pipeline** – Jenkins pipeline that builds, tests, creates Docker images, pushes to Docker Hub, loads images into kind, and applies rolling updates on Kubernetes.

All services expose Prometheus metrics (`/actuator/prometheus`), health endpoints, and are scraped by Prometheus. Logs are collected from all namespaces and are queryable in Grafana.

---

## 🏗 Architecture

┌─────────────────────────────────────────────────────────────┐
│ Ubuntu 24.04 VM │
│ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ kind cluster │ │
│ │ ┌─────────────────────────────────────────────┐ │ │
│ │ │ devops-platform namespace │ │ │
│ │ │ user-service (8081) │ │ │
│ │ │ product-service (8082) │ │ │
│ │ │ order-service (8083) │ │ │
│ │ └─────────────────────────────────────────────┘ │ │
│ │ │ │
│ │ monitoring namespace │ │
│ │ Prometheus │ Grafana │ Loki │ Promtail │ │
│ └─────────────────────────────────────────────────────┘ │
│ │
│ ┌──────────────┐ ┌──────────────┐ │
│ │ Jenkins │ │ Docker Hub │ │
│ │ container │◄──►│ registry │ │
│ └──────────────┘ └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
text


---

## ✅ Prerequisites

- **Ubuntu 24.04** (or any Linux with Docker)
- **Git** – clone the repository
- **Docker** – for kind and Jenkins container
- **kubectl** – command line for Kubernetes
- **kind** – Kubernetes in Docker
- **Java 21** & **Maven** – to build the microservices
- **Helm** – to install Prometheus/Grafana/Loki
- **curl** – for testing endpoints

---

## 🔧 Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/Islem-rb/devops-platform.git
cd devops-platform

2. Create kind cluster
bash

kind create cluster --config kind-cluster.yaml --name devops-platform

The kind-cluster.yaml file defines 1 control‑plane and 2 worker nodes.
3. Build and load microservice images (first time)

Each service has its own Dockerfile. Build and load them into kind manually for the initial deployment:
bash

cd user-service && docker build -t devops-platform/user-service:latest . && cd ..
cd product-service && docker build -t devops-platform/product-service:latest . && cd ..
cd order-service && docker build -t devops-platform/order-service:latest . && cd ..

kind load docker-image devops-platform/user-service:latest --name devops-platform
kind load docker-image devops-platform/product-service:latest --name devops-platform
kind load docker-image devops-platform/order-service:latest --name devops-platform

4. Deploy the microservices
bash

kubectl apply -f k8s/user-service.yaml
kubectl apply -f k8s/product-service.yaml
kubectl apply -f k8s/order-service.yaml

Wait for all pods to be Running:
bash

kubectl get pods -n devops-platform -w

5. Install observability stack
Prometheus & Grafana
bash

helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm install monitoring prometheus-community/kube-prometheus-stack -n monitoring --create-namespace -f k8s/prometheus-values.yaml

Grafana default credentials: admin / devops123
Loki & Promtail
bash

helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
helm install loki grafana/loki-stack -n monitoring --create-namespace -f k8s/loki-stack-values.yaml

Apply RBAC fix for Promtail:
bash

kubectl apply -f k8s/promtail-rbac.yaml

6. Access dashboards

Use port‑forward to access services (run in separate terminals):
bash

# Grafana
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80 --address=0.0.0.0

# Prometheus
kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090 --address=0.0.0.0

Open your browser:

    Grafana → http://<VM_IP>:3000

        Login: admin / devops123

        Loki datasource already configured (http://loki:3100)

        Import dashboard ID 315 for Kubernetes cluster monitoring

        Custom dashboard “DevOps Platform — Services Overview” includes:

            Request rate per service

            Error rate (5xx) per service

            JVM memory used per service

            CPU usage per pod

    Prometheus → http://<VM_IP>:9090

7. Jenkins CI/CD Pipeline

We run Jenkins as a Docker container on the VM with host networking and the Docker socket mounted, so it can build images and communicate with the kind cluster.
Start Jenkins
bash

docker run -d --name jenkins \
  --restart=unless-stopped \
  --network host \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v $HOME/.kube:/root/.kube \
  -u root \
  jenkins/jenkins:lts

Get the initial admin password:
bash

docker logs jenkins 2>&1 | grep -A 5 "Please use the following password"

Access Jenkins at http://<VM_IP>:8080 and complete the setup (install suggested plugins, create admin user admin/devops123).
Install additional plugins

    Docker Pipeline

    Kubernetes CLI

    Blue Ocean

Add Docker Hub credentials

    ID: dockerhub-credentials

    Username: your Docker Hub username (islemrb)

    Password: your Docker Hub password or access token

Create a Pipeline job

    Name: devops-platform-pipeline

    Definition: Pipeline script from SCM

    SCM: Git

    Repository URL: https://github.com/Islem-rb/devops-platform.git

    Branch: main

    Script Path: Jenkinsfile

The Jenkinsfile defines the following stages:

    Checkout – pulls the latest code from GitHub.

    Build & Test – runs mvn clean package for each service.

    Docker Build & Push – builds images with latest and ${BUILD_NUMBER} tags, pushes to Docker Hub.

    Load Images into Kind – loads the tagged images into the kind cluster (required because imagePullPolicy: Never).

    Deploy to Kubernetes – updates each deployment with the new image tag and waits for rollout.

Every push to main (or manual build) triggers the pipeline. Rolling updates ensure zero downtime.
🧪 Testing

After a successful pipeline run, you can test the endpoints:
bash

kubectl port-forward -n devops-platform svc/user-service 8081:8081 &
curl http://localhost:8081/users

kubectl port-forward -n devops-platform svc/product-service 8082:8082 &
curl http://localhost:8082/products

kubectl port-forward -n devops-platform svc/order-service 8083:8083 &
curl -X POST http://localhost:8083/orders -H "Content-Type: application/json" -d '{"productId":1,"quantity":2}'

Check logs in Grafana: Explore → select Loki datasource → query {namespace="devops-platform"}.
🛠 Troubleshooting
inotify limits for Promtail

If you see “too many open files” errors, apply on the host and inside each kind node:
bash

sudo sysctl fs.inotify.max_user_instances=512
sudo sysctl fs.inotify.max_user_watches=524288

for node in $(kind get nodes --name devops-platform); do
  docker exec $node sysctl fs.inotify.max_user_instances=512
  docker exec $node sysctl fs.inotify.max_user_watches=524288
done

Jenkins cannot connect to kind

Make sure $HOME/.kube is mounted into the Jenkins container (as shown above) and that the kind cluster is accessible:
bash

docker exec jenkins kubectl get nodes

Maven not found in Jenkins

The container includes Maven if you installed it. If not, exec into the container and run:
bash

apt-get update && apt-get install -y maven

📈 Future Improvements

    Canary deployments – update only a percentage of pods, validate metrics, then full rollout.

    Rollback stage – automatically revert to previous version if health checks fail.

    Slack/Email notifications on pipeline success/failure.

    Performance tests integrated into the pipeline (e.g., JMeter).

    Service mesh (Istio) for advanced traffic management.

🧑‍💻 Author

Islem Rebhi – GitHub
📝 License

This project is for educational purposes.
