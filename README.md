DevOps Observability Platform -
README

�� DevOps Observability Platform
A complete, production-ready DevOps observability ecosystem deployed on Kubernetes
(**kind**). This platform features three Spring Boot microservices, a comprehensive
observability stack, and an automated Jenkins CI/CD pipeline.
---
�� Project Overview
* **Microservices:** User, Product, and Order services (Spring Boot 3.5.0, Java 21).
* **Infrastructure:** Kubernetes (kind) with 1 control plane and 2 worker nodes.
* **Observability:** Prometheus (metrics), Grafana (visualization), Loki + Promtail (log
aggregation).
* **CI/CD:** Jenkins pipeline for automated build, test, push, and rolling updates.
---
�� Architecture Diagram
[Insert Diagram Here]
---
⚙️ Prerequisites
Ensure your environment is ready:
* **OS:** Ubuntu 24.04 (or Linux with Docker).
* **Core:** Git, Docker, kubectl, kind, Helm.
* **Languages:** Java 21 &amp; Maven.
---
�� Setup Instructions
### 1. Initialize Project
git clone https://github.com/Islem-rb/devops-platform.git
cd devops-platform

### 2. Create Kind Cluster
kind create cluster --config kind-cluster.yaml --name devops-platform
### 3. Build &amp; Load Images
For the initial deployment, build and push to the kind cluster:

Build
cd user-service &amp;&amp; docker build -t devops-platform/user-service:latest . &amp;&amp; cd ..
cd product-service &amp;&amp; docker build -t devops-platform/product-service:latest . &amp;&amp; cd ..
cd order-service &amp;&amp; docker build -t devops-platform/order-service:latest . &amp;&amp; cd ..

Load
kind load docker-image devops-platform/user-service:latest --name devops-platform
kind load docker-image devops-platform/product-service:latest --name devops-platform
kind load docker-image devops-platform/order-service:latest --name devops-platform
### 4. Deploy Microservices
kubectl apply -f k8s/user-service.yaml
kubectl apply -f k8s/product-service.yaml
kubectl apply -f k8s/order-service.yaml

Monitor rollout
kubectl get pods -n devops-platform -w
### 5. Install Observability Stack
**Prometheus &amp; Grafana:**
helm repo add prometheus-community https://prometheus-community.github.io/helm-
charts
helm install monitoring prometheus-community/kube-prometheus-stack -n monitoring --
create-namespace -f k8s/prometheus-values.yaml
**Loki &amp; Promtail:**
helm repo add grafana https://grafana.github.io/helm-charts

helm install loki grafana/loki-stack -n monitoring --create-namespace -f k8s/loki-stack-
values.yaml
kubectl apply -f k8s/promtail-rbac.yaml
---
�� Accessing Dashboards
Access via port-forwarding:
| Service | Command | URL |
| :--- | :--- | :--- |
| **Grafana** | kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80 |
http://&lt;VM_IP&gt;:3000 |
| **Prometheus** | kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-
prometheus 9090:9090 | http://&lt;VM_IP&gt;:9090 |
* **Login:** admin / devops123
* **Grafana Setup:** Use Loki datasource (http://loki:3100) and import Dashboard ID: 315.
---
⚙️ Jenkins CI/CD Pipeline
Start Jenkins with the necessary Docker and K8s bindings:
docker run -d --name jenkins --restart=unless-stopped --network host \
-v jenkins_home:/var/jenkins_home \
-v /var/run/docker.sock:/var/run/docker.sock \
-v $HOME/.kube:/root/.kube \
-u root jenkins/jenkins:lts
* **Plugins Required:** Docker Pipeline, Kubernetes CLI, Blue Ocean.
* **Credential ID:** dockerhub-credentials
---
�� Troubleshooting
&gt; &quot;Too many open files&quot; (Promtail):
&gt; Increase inotify limits on the host and inside kind nodes:

&gt; sudo sysctl fs.inotify.max_user_instances=512
&gt; sudo sysctl fs.inotify.max_user_watches=524288
&gt; &quot;Jenkins cannot connect to kind&quot;:
&gt; Ensure $HOME/.kube is mounted. Verify with: docker exec jenkins kubectl get nodes.
---
�� Future Roadmap
* **Deployment:** Implement Canary deployments and automatic rollbacks.
* **Alerting:** Slack/Email notifications on pipeline failures.
* **Load Testing:** Integrate JMeter into the pipeline.
* **Traffic:** Implement Istio for service mesh capabilities.
---
Author: Islem Rebhi | Educational Project
