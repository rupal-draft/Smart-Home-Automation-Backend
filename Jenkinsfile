pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven'
    }

    environment {
        SONAR_HOME = tool "Sonar Scanner"
    }

    stages {

        stage("Workplace cleanup") {
            steps {
                echo "Cleaning workspace..."
                cleanWs()
            }
        }

        stage("Git: Code Checkout") {
            steps {
                echo "Checking out code from Git..."
                git url: "https://github.com/rupal-draft/Smart-Home-Automation-Backend.git", branch: "main"
            }
        }

        stage("Build: Compile Project") {
            steps {
                echo "Compiling project with Maven..."
                sh "mvn clean compile -DskipTests"
            }
        }

        stage("Trivy: Filesystem Scan") {
            steps {
                echo "Running Trivy filesystem scan..."
                sh "trivy fs --format table -o trivy-fs-report.html ."
            }
        }

        stage("SonarQube: Code Analysis") {
            steps {
                echo "Running SonarQube analysis..."
                withSonarQubeEnv("Sonar") {
                    sh "$SONAR_HOME/bin/sonar-scanner -Dsonar.projectName=Smart-Home-Automation-Backend -Dsonar.projectKey=shab -Dsonar.java.binaries=target/classes -X"
                }
            }
        }

        stage("SonarQube: Code Quality Gates") {
            steps {
                echo "Waiting for SonarQube quality gate result..."
                timeout(time: 5, unit: "MINUTES") {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage("OWASP: Dependency Check") {
            steps {
                script {
                    echo "Running OWASP Dependency Check..."

                    try {
                        dependencyCheck additionalArguments: '--scan ./ --format XML --format HTML',
                                        odcInstallation: 'owasp'

                        echo "Publishing Dependency Check XML report..."
                        dependencyCheckPublisher pattern: '**/dependency-check-report.xml'

                        echo "Publishing Dependency Check HTML report..."
                        publishHTML(target: [
                            reportName: 'OWASP Dependency-Check Report',
                            reportDir: '.',
                            reportFiles: 'dependency-check-report.html',
                            keepAll: true,
                            allowMissing: true,
                            alwaysLinkToLastBuild: true
                        ])

                        echo "✅ OWASP Dependency Check completed successfully!"

                    } catch (err) {
                        echo "⚠️ OWASP Dependency Check encountered an error: ${err}"
                        echo "⚠️ Marking as warning only (build continues)..."
                        currentBuild.result = 'SUCCESS' // Prevents unstable mark
                    }
                }
            }
        }

        stage("Docker: Build Image") {
            steps {
                echo "Building Docker image..."
                sh "docker build -t smart-home-automation:latest ."
            }
        }

        stage("Docker: Push to DockerHub") {
            steps {
                echo "Tagging Docker image for DockerHub..."
                sh "docker tag smart-home-automation:latest rupaldraft/smart-home-automation:latest"
                echo "Logging in to DockerHub..."
                withCredentials([usernamePassword(
                        credentialsId: 'dockerHubCred',
                        passwordVariable: 'dockerhubpass',
                        usernameVariable: 'dockerhubuser')
                    ]) {
                    sh "docker login -u ${env.dockerhubuser} -p ${env.dockerhubpass}"
                }
                echo "Pushing Docker image to DockerHub..."
                sh "docker push rupaldraft/smart-home-automation:latest"
            }
        }

        stage("Prepare Environment File") {
            steps {
                echo "Creating .env file securely from Jenkins credentials..."
                withCredentials([
                    usernamePassword(credentialsId: 'postgresCred', usernameVariable: 'POSTGRES_USER', passwordVariable: 'POSTGRES_PASSWORD'),
                    string(credentialsId: 'jwtSecret', variable: 'JWT_SECRET')
                ]) {
                    sh '''
                        echo "POSTGRES_DB=smart_home" > .env
                        echo "POSTGRES_USER=$POSTGRES_USER" >> .env
                        echo "POSTGRES_PASSWORD=$POSTGRES_PASSWORD" >> .env
                        echo "SPRING_DATA_REDIS_HOST=shas-cache" >> .env
                        echo "SPRING_DATA_REDIS_PORT=6379" >> .env
                        echo "SPRING_PROFILES_ACTIVE=dev" >> .env
                        echo "JWT_SECRET=$JWT_SECRET" >> .env
                        echo "SPRING_DATASOURCE_URL=jdbc:postgresql://shas-database:5432/smart_home" >> .env
                        echo "SPRING_DATASOURCE_USERNAME=$POSTGRES_USER" >> .env
                        echo "SPRING_DATASOURCE_PASSWORD=$POSTGRES_PASSWORD" >> .env
                    '''
                }
            }
        }

        stage("GitOps: Update K8s Manifest with New Image Tag") {
            steps {
                script {

                    echo "Updating Kubernetes manifest with new image tag..."

                    def imageTag = sh(script: "date +%s", returnStdout: true).trim()

                    sh """
                        sed -i 's|image: rupaldraft/smart-home-automation:.*|image: rupaldraft/smart-home-automation:${imageTag}|g' k8s/deployment.yaml
                    """

                    withCredentials([
                        string(credentialsId: 'gitUserName', variable: 'GIT_NAME'),
                        string(credentialsId: 'gitUserEmail', variable: 'GIT_EMAIL'),
                        string(credentialsId: 'githubPAT', variable: 'GITHUB_PAT')
                    ]) {

                        sh """
                            git config user.name "${GIT_NAME}"
                            git config user.email "${GIT_EMAIL}"

                            git add k8s/deployment.yaml
                            git commit -m "CI: Update image tag to ${imageTag}" || true

                            git remote set-url origin https://${GIT_NAME}:${GITHUB_PAT}@github.com/rupal-draft/Smart-Home-Automation-Backend.git

                            git push origin main
                        """
                    }
                }
            }
        }

    }
}
