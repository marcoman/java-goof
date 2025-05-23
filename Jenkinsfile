pipeline {
    agent any

    environment {
        // Define Artifactory URL
        ARTIFACTORY_SERVER_URL = 'https://soleng.jfrog.io'
        ARTIFACTORY_ID = "soleng.jfrog.io"
        // For this demonstration, this is my username
        ARTIFACTORY_PROJECT_ID = 'marcom'
        // This next line defines my virtual maven repository for all maven dependency pull/push.
        ARTIFACTORY_MVN_REPOS = 'marco-maven-dev-virtual'
        BUILD_NAME = "${env.JOB_NAME}"
        BUILD_NUMBER = "${env.BUILD_NUMBER}"
        BUILD_URL = "${env.BUILD_URL}"
        JFROG_CLI_BUILD_NAME = "${env.JOB_NAME}"
        JFROG_CLI_BUILD_NUMBER = "${env.BUILD_NUMBER}"
        DOCKER_REPO = "marco-dev-docker"
        DOCKER_IMAGE_NAME = "log4shell-server:${env.BUILD_NUMBER}"
    }

    tools {
        jdk "OpenJDK-11"
        maven "maven-3.9.9"
        jfrog "jfrog-cli"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/marcoman/java-goof.git'
            }
        }
        stage('Acquire JFrog CLI') {
            steps {
                // Download and install JFrog CLI, and show the version for diagnostic purposes.
                script {
                    sh '''
                        echo "Installing JFrog CLI..."
#                        curl -fL https://install-cli.jfrog.io | sh                        
#                        chmod +x jfrog
                    '''
                    
                    sh '''
                    jf --version
                    '''
                    
                }
            }
        }
        stage('Setup JFrog CLI') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'ARTIFACTORY_CREDENTIALS_ID',
                        usernameVariable: 'ARTIFACTORY_USERNAME',
                        passwordVariable: 'ARTIFACTORY_PASSWORD'
                    )
                ]) {
                    // Your pipeline steps that use the USERNAME and PASSWORD variables
                    // Login to Artifactory using JFrog CLI with config command
                    sh '''
                        #jf config add my-artifactory --url=${ARTIFACTORY_SERVER_URL} --user=${ARTIFACTORY_USERNAME} --password=${ARTIFACTORY_PASSWORD}
                        #--interactive=false
                        #\
                        #--overwrite=true'
                        echo "SKIPPING Artifactory definition."
                    '''
                    
                    // configure our routes to maven
                    sh '''
                        jf mvn-config --repo-deploy-releases ${ARTIFACTORY_MVN_REPOS} --repo-deploy-snapshots ${ARTIFACTORY_MVN_REPOS} --repo-resolve-releases ${ARTIFACTORY_MVN_REPOS} --repo-resolve-snapshots ${ARTIFACTORY_MVN_REPOS}
                    '''

                    // show results of ping to the server
                    sh '''
                    jf rt ping
                    '''
                    
                    // show the configuration as we've defined it.
                    sh '''
                    jf config show
                    '''
                    
                    sh '''
                    echo "Show the maven.yaml contents"
                    cat .jfrog/projects/maven.yaml
                    '''
                    
                    // Logon to docker
                    sh '''
                    docker login soleng.jfrog.io -u ${ARTIFACTORY_USERNAME} -p ${ARTIFACTORY_PASSWORD}
                    '''
                }
            }
        }

        stage('Build') {
            steps {
                sh '''
                echo "show maven version"
                mvn --version
                jf mvn clean package
                #mvn clean package
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                echo "deploy"
                jf mvn deploy
                #mvn deploy
                '''
            }
        }
        
        stage('Build Docker') {
            steps {
                dir("log4shell-goof/log4shell-server") {
                    sh '''
                    docker buildx ls
                    echo "show version"
                    jf docker --version
                    echo "build docker image"
                    jf docker build --tag ${ARTIFACTORY_ID}/${DOCKER_REPO}/${DOCKER_IMAGE_NAME} --platform linux/amd64 --metadata-file metadata.json .
                    echo "docker push ${ARTIFACTORY_ID}/${DOCKER_REPO}/${DOCKER_IMAGE_NAME}"
                    jf docker push ${ARTIFACTORY_ID}/${DOCKER_REPO}/${DOCKER_IMAGE_NAME} --project marco
                    echo "finish docker push."
                    '''
                }
            }
        }
        
        
        stage('🐸 JFrog Curation Audit') {
            steps {
                sh '''
                echo "Curation"
                jf curation-audit --threads 6
                '''
            }
        }

        stage('🐸 JFrog Secrets Scan') {
            steps {
                sh '''
                echo "Secrets"
                jf audit --secrets --validate-secrets true
                '''
            }
        }

        stage('🐸 JFrog SAST Scan') {
            steps {
                sh '''
                echo "SAST"
                jf audit --sast --format=sarif --fail false > jfrog_sast.sarif
                '''
            }
        }

        stage('🐸 JFrog Collect Environment') {
            steps {
                sh '''
                echo "**** COLLECT ENVIRONMENT ****"
                #jf rt build-collect-env ${BUILD_NAME} ${BUILD_NUMBER}
                jf rt build-collect-env
                '''
            }
        }

        stage('🐸 JFrog Add Dependencies') {
            steps {
                sh '''
                echo "**** ADD DEPENDENCIES ****"
                #jf rt build-add-dependencies ${BUILD_NAME} ${BUILD_NUMBER} .
                jf rt build-add-dependencies .
                '''
            }
        }

        stage('🐸 JFrog Add Git') {
            steps {
                sh '''
                echo "**** ADD GIT ****"
                 #jf rt build-add-git
                 jf rt build-add-git
                '''
            }
        }
        stage('🐸 JFrog Publish Build Info') {
            steps {
                sh '''
                echo "**** PUBLISH BUILD INFO ****"
                 #jf rt build-publish --build-url ${env.BUILD_URL}
                 #jf rt build-publish --build-url "${BUILD_URL}" ${BUILD_NAME} ${BUILD_NUMBER}
                 jf rt build-publish --build-url "${BUILD_URL}"
                '''
            }
        }
    }
}
