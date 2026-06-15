# DevOps Observability Platform

A complete observability platform with three Spring Boot microservices, Prometheus/Grafana/Loki stack, and a Jenkins CI/CD pipeline on kind Kubernetes.

---

## Overview

This project demonstrates a production-like DevOps observability platform running on **kind** (Kubernetes in Docker). It includes:

- Three independent Spring Boot microservices (User, Product, Order)
- Full observability: metrics (Prometheus), dashboards (Grafana), logs (Loki + Promtail)
- CI/CD pipeline with Jenkins that builds, tests, pushes Docker images, loads them into kind, and performs rolling updates

---

## Tech Stack

| Category | Technologies |
|----------|--------------|
| Microservices | Spring Boot 3.5.0, Java 21, Maven |
| Containerization | Docker, kind |
| Orchestration | Kubernetes (Deployments, Services) |
| Observability | Prometheus, Grafana, Loki, Promtail |
| CI/CD | Jenkins, GitHub, Docker Hub |
| VM Environment | Ubuntu 24.04, kubectl, Helm, Git |

---

## Prerequisites

Install on Ubuntu 24.04:

- Docker
- kubectl
- kind
- Helm
- Git
- Java 21 & Maven
- curl

Set inotify limits:

```bash
sudo sysctl fs.inotify.max_user_instances=512
sudo sysctl fs.inotify.max_user_watches=524288

Setup Instructions
1. Clone Repository
bash

git clone https://github.com/Islem-rb/devops-platform.git
cd devops-platform

2. Create kind Cluster
bash

kind create cluster --config kind-cluster.yaml --name devops-platform

3. Build & Load Initial Images
bash

cd user-service && docker build -t devops-platform/user-service:latest . && cd ..
cd product-service && docker build -t devops-platform/product-service:latest . && cd ..
cd order-service && docker build -t devops-platform/order-service:latest . && cd ..

kind load docker-image devops-platform/user-service:latest --name devops-platform
kind load docker-image devops-platform/product-service:latest --name devops-platform
kind load docker-image devops-platform/order-service:latest --name devops-platform

4. Deploy Microservices
bash

kubectl apply -f k8s/user-service.yaml
kubectl apply -f k8s/product-service.yaml
kubectl apply -f k8s/order-service.yaml

Check pods:
bash

kubectl get pods -n devops-platform -w

5. Install Observability Stack

Prometheus + Grafana:
bash

helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm install monitoring prometheus-community/kube-prometheus-stack -n monitoring --create-namespace -f k8s/prometheus-values.yaml

Grafana login: admin / devops123

Loki + Promtail:
bash

helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
helm install loki grafana/loki-stack -n monitoring --create-namespace -f k8s/loki-stack-values.yaml
kubectl apply -f k8s/promtail-rbac.yaml

6. Access Dashboards

Port-forward (run in separate terminals):
bash

kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80 --address=0.0.0.0
kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090 --address=0.0.0.0

Open browser:

    Grafana: http://<VM_IP>:3000 (admin/devops123)

    Prometheus: http://<VM_IP>:9090

7. Setup Jenkins CI/CD

Start Jenkins container:
bash

docker run -d --name jenkins \
  --restart=unless-stopped \
  --network host \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v $HOME/.kube:/root/.kube \
  -u root \
  jenkins/jenkins:lts

Get initial password:
bash

docker logs jenkins 2>&1 | grep -A 5 "Please use the following password"

Access Jenkins at http://<VM_IP>:8080 and complete setup (install suggested plugins, create admin user).

Install additional plugins: Docker Pipeline, Kubernetes CLI, Blue Ocean.

Add Docker Hub credentials: ID dockerhub-credentials with your username/password.

Create Pipeline job: Use SCM Git, repo https://github.com/Islem-rb/devops-platform.git, branch main, script path Jenkinsfile.

Run the pipeline – stages: Checkout → Build & Test → Docker Build & Push → Load Images into Kind → Deploy to Kubernetes.
Testing

After pipeline succeeds, test endpoints:
bash

kubectl port-forward -n devops-platform svc/user-service 8081:8081 &
kubectl port-forward -n devops-platform svc/product-service 8082:8082 &
kubectl port-forward -n devops-platform svc/order-service 8083:8083 &

curl http://localhost:8081/users
curl http://localhost:8082/products
curl -X POST http://localhost:8083/orders -H "Content-Type: application/json" -d '{"productId":1,"quantity":2}'

View logs in Grafana: Explore → Loki → query {namespace="devops-platform"}.
Troubleshooting

inotify limits on kind nodes:
bash

for node in $(kind get nodes --name devops-platform); do
  docker exec $node sysctl fs.inotify.max_user_instances=512
  docker exec $node sysctl fs.inotify.max_user_watches=524288
done

Jenkins cannot connect to kind:
bash

docker exec jenkins kubectl get nodes

Maven not found in Jenkins:
bash

docker exec -it -u root jenkins bash
apt-get update && apt-get install -y maven
exit

Future Improvements

    Canary deployments

    Automatic rollback on failure

    Slack/email notifications

    Performance tests in pipeline

    Service mesh (Istio)

Author

Islem Rebhi – GitHub
License

Educational purposes only.
