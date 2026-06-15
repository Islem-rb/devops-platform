pipeline {
    agent any

    environment {
        DOCKERHUB_USER = 'islemrb'
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        SERVICES = 'user-service product-service order-service'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    def services = env.SERVICES.split(' ')
                    for (service in services) {
                        echo "Building ${service}..."
                        dir(service) {
                            sh './mvnw clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    def services = env.SERVICES.split(' ')
                    for (service in services) {
                        echo "Testing ${service}..."
                        dir(service) {
                            sh './mvnw test'
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
                        echo "Building Docker image for ${service}..."
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

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    def services = env.SERVICES.split(' ')
                    for (service in services) {
                        echo "Deploying ${service} to Kubernetes..."
                        sh "kubectl set image deployment/${service} ${service}=${DOCKERHUB_USER}/${service}:${BUILD_NUMBER} -n devops-platform"
                        sh "kubectl rollout status deployment/${service} -n devops-platform --timeout=120s"
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully! All services deployed.'
        }
        failure {
            echo 'Pipeline failed! Check the logs above.'
        }
    }
}
