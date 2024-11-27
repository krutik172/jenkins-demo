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
                    sh 'docker run --rm -d --name demo-app -p 8089:8089 demo-app'
                }
            }
        }
       stage('Health Check') {
           steps {
               script {
                   echo "Performing health check by executing a command inside the container..."
                   def retries = 5
                   def healthCheckSuccess = false
                   for (int i = 0; i < retries; i++) {
                       try {
                           // Execute a curl inside the container (no need to have curl installed on the host)
                           def response = sh(script: "docker exec demo-app curl -s -o /dev/null -w '%{http_code}' http://localhost:8089/health", returnStdout: true).trim()
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
                       sh 'docker stop demo-app || true'
                       sh 'docker rm demo-app || true'
                       error("Rollback triggered due to health check failure.")
                   }
               }
           }
       }
    }
}
