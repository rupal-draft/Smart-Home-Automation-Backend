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
                        dependencyCheck additionalArguments: '--scan ./ --format XML --format HTML --disableRetireJs',
                                        odcInstallation: 'owasp'

                        echo "Publishing Dependency Check XML report..."
                        dependencyCheckPublisher pattern: '**/dependency-check-report.xml'

                        echo "Publishing Dependency Check HTML report..."
                        publishHTML(target: [
                            reportName: 'OWASP Dependency-Check Report',
                            reportDir: '.',
                            reportFiles: 'dependency-check-report.html',
                            keepAll: true,
                            allowMissing: false,
                            alwaysLinkToLastBuild: true
                        ])

                        echo "OWASP Dependency Check completed successfully!"

                    } catch (err) {
                        echo "⚠️ OWASP Dependency Check encountered an error: ${err}"
                        echo "Reports may be incomplete, but pipeline continues."
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

        stage("Docker: Deploy with Docker Compose") {
            steps {
                echo "Stopping and starting containers using Docker Compose..."
                sh "docker-compose down && docker-compose --env-file .env up -d"
            }
        }

    }
}
