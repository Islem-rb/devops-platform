pipeline {
    agent any

    environment {
        DOCKERHUB_USER = 'islemrb'
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        SERVICES = 'user-service product-service order-service'
        KIND_CLUSTER_NAME = 'devops-platform'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    def services = env.SERVICES.split(' ')
                    for (service in services) {
                        echo "Building and testing ${service}..."
                        dir(service) {
                            sh 'mvn clean package'
                        }
                    }
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                    def services = env.SERVICES.split(' ')
                    for (service in services) {
                        dir(service) {
                            sh "docker build -t ${DOCKERHUB_USER}/${service}:latest ."
                            sh "docker build -t ${DOCKERHUB_USER}/${service}:${BUILD_NUMBER} ."
                            sh "docker push ${DOCKERHUB_USER}/${service}:latest"
                            sh "docker push ${DOCKERHUB_USER}/${service}:${BUILD_NUMBER}"
                        }
                    }
                }
            }
        }

        stage('Load Images into Kind') {
            steps {
                script {
                    def services = env.SERVICES.split(' ')
                    for (service in services) {
                        echo "Loading ${DOCKERHUB_USER}/${service}:${BUILD_NUMBER} into kind..."
                        sh "kind load docker-image ${DOCKERHUB_USER}/${service}:${BUILD_NUMBER} --name ${KIND_CLUSTER_NAME}"
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    def services = env.SERVICES.split(' ')
                    for (service in services) {
                        echo "Updating image for ${service}..."
                        sh "kubectl set image deployment/${service} ${service}=${DOCKERHUB_USER}/${service}:${BUILD_NUMBER} -n devops-platform"
                        sh "kubectl rollout status deployment/${service} -n devops-platform --timeout=120s"
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check the logs.'
        }
    }
}
