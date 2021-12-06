def call (body) {
   def config = [:]
   body.resolveStrategy = Closure.DELEGATE_FIRST
   body.delegate = config
   body();

   String ChangeLog = '';
   String RepoUrl = config.repoUrl;
   String RepoBranch = config.repoBranch;
   String DockerFileFolder = config.dockerFileFolder;
   String DockerRegistry = config.dockerRegistry;
   String DockerRegistryCredentials = config.dockerRegistryCredentials; 

   pipeline {
      agent any
      stages {
        stage('Checkout') {
            steps {
                // Clean before build
                cleanWs();
                echo "Workspace is ${Workspace}";
                git branch: "${RepoBranch}", url: "${RepoUrl}" 
                
            }
        }   
        stage('Docker Build') {
            steps {
                dir("${DockerFileFolder}") {
                    powershell(script: """
                        docker images -a
                        docker build -t ${DockerRegistry} .
                        docker images -a 
                        """)                         
                }
            }
        }
        stage('Push Container') {
            steps { 
                dir("${DockerFileFolder}") { 
                    script {
                        docker.withRegistry("https://index.docker.io/v1/", DockerRegistryCredentials)
                        {
                            def image = docker.build(DockerRegistry) 
                            image.push(); 
                        } 
                    }
                }
            }
        }
        stage('Container Scanning')
        {
            parallel {
                // stage('Run Anchore') {
                //     steps {
                //         sleep(time: 10, unit: 'SECONDS')
                //         powershell(script: """
                //             Write-Output: ${DockerRegistry} 
                //         """)
                //         }
                // }
                stage('Run Trivy') {
                    steps {
                        powershell(script: """
                            docker pull aquasec/trivy:0.21.1 
                        """)
                        powershell(script: """
                            docker run --rm -v C:/root/.cache/ aquasec/trivy:0.21.1 ${DockerRegistry}
                                """)
                    }
                }
            }
        }
        stage('Deploy to QA') {
            environment {
                ENVIRONMENT ='qa'
            }
            steps {
                echo "Deploying to ${ENVIRONMENT}"
            }
        }
        stage('Approve PROD Deploy') { 
            when {
                not {
                branch 'master'
                }
            }
            options {
                timeout(time:1, unit:'HOURS')
            }
            steps {
                input message: "Deploy ?"
            }
            post {
                success {
                echo "Production Deploy Approved" 
                }
                aborted {
                echo "Production Deploy Denied" 
                }
            }
        }
        stage('Deploy to PROD') { 
            when {
                not {
                branch 'master'
                }
            }
            environment {
                ENVIRONMENT = 'prod'
            }
            steps {
                echo "Deploying to ${ENVIRONMENT}" 
            }
        }


      }
   }
}