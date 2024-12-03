pipeline {
    agent any
    tools {
        jdk 'JDK21'
    }
    environment {
        MAX_RETRIES = 5
        HEALTH_CHECK_URL = "http://localhost:8089/health"
        BACKUP_IMAGE = "demo-app-stable"
        NOTIFICATION_EMAIL = "team@example.com"
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean install'
                sh 'docker build -t demo-app .'
            }
        }
        stage('Deploy') {
            steps {
                script {
                    echo "Checking for existing processes on port 8089..."
                    sh 'lsof -i :8089 | grep LISTEN && kill -9 $(lsof -t -i :8089) || true'
                    echo "Deploying the application..."
                    sh 'docker stop demo-app || true'
                    sh 'docker rm demo-app || true'
                    sh 'docker run --rm -d --name demo-app -p 8089:8089 demo-app'
                }
            }
        }
        stage('Health Check') {
            steps {
                script {
                    echo "Performing health check by executing a command inside the container..."
                    def retries = env.MAX_RETRIES.toInteger()
                    def healthCheckSuccess = false

                    for (int i = 0; i < retries; i++) {
                        try {
                            def response = sh(script: "curl -s -o /dev/null -w '%{http_code}' ${env.HEALTH_CHECK_URL}", returnStdout: true).trim()
                            if (response == '200') {
                                echo "Health check passed."
                                healthCheckSuccess = true
                                break
                            } else {
                                echo "Health check failed, retrying... (${i+1}/$retries)"
                                sleep(5)
                            }
                        } catch (Exception e) {
                            echo "Error during health check: ${e.getMessage()}"
                            sleep(5)
                        }
                    }

                    if (!healthCheckSuccess) {
                        echo "Health check failed after $retries retries. Rolling back..."
                        performRollback()
                        error("Pipeline failed: Rollback triggered due to health check failure.")
                    }
                }
            }
        }
        stage('Fallback Deploy') {
            when {
                expression {
                    currentBuild.result == 'FAILURE'
                }
            }
            steps {
                script {
                    echo "Deploying fallback version..."
                    sh 'docker stop demo-app || true'
                    sh 'docker rm demo-app || true'
                    sh "docker run --rm -d --name demo-app -p 8089:8089 ${env.BACKUP_IMAGE}"
                }
            }
        }
    }
    post {
        always {
            script {
                archiveArtifacts artifacts: '**/target/*.jar', onlyIfSuccessful: true
                cleanWs()
            }
        }
        failure {
            script {
                notifyFailure(env.NOTIFICATION_EMAIL)
            }
        }
    }
}

def performRollback() {
    echo "Stopping and removing the application container..."
    sh 'docker stop demo-app || true'
    sh 'docker rm demo-app || true'
}

def notifyFailure(email) {
    echo "Sending failure notification to ${email}..."
}
