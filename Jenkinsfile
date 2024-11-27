pipeline {
    agent any
       tools {
            jdk 'JDK21'
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
                    sh 'docker run --rm -d --name demo-app -p 8089:8080 demo-app'
                }
            }
        }
        stage('Health Check') {
            steps {
                script {
                    echo "Performing health check..."
                    def response = sh(script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:8089/health", returnStdout: true).trim()
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
