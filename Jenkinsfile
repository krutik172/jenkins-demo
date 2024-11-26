pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package'
                sh 'docker build -t demo-app .'
            }
        }
        stage('Deploy') {
            steps {
                script {
                    echo "Deploying the application..."
                    sh 'docker stop demo-app || true'
                    sh 'docker rm demo-app || true'
                    sh 'docker run -d --name demo-app -p 8090:8080 demo-app'
                }
            }
        }
        stage('Health Check') {
            steps {
                script {
                    echo "Performing health check..."
                    def response = sh(script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:8090/health", returnStdout: true).trim()
                    if (response != '200') {
                        echo "Health check failed. Rolling back..."
                        sh 'docker stop demo-app || true'
                        sh 'docker rm demo-app || true'
                        error("Rollback triggered due to health check failure.")
                    } else {
                        echo "Health check passed."
                    }
                }
            }
        }
    }
}
