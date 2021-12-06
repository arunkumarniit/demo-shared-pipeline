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
   String TestsContainerFileLocation = config.testsContainerFileLocation;
   String TestsContainerFileName = config.testsContainerFileName;
   String TestsScriptsFileLocation = config.testsScriptsFileLocation;
   String TestsScriptsFileName = config.testsScriptsFileName; 

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
        stage('Start test app') {
            steps {
                dir("${TestsContainerFileLocation}") {
                    powershell(script: """ 
                        \$File = Join-Path -Path (Get-Location) -ChildPath "${TestsContainerFileLocation}\\${TestsContainerFileName}"
                        \$File
                        docker-compose up -d  \$File
                    """)  
                }
         }
         post {
            success {
               echo "App started successfully :)"
            }
            failure {
               echo "App failed to start :("
            }
         }
      }
      stage('Run Tests') {
         steps { 
              dir("${Workspace}\\${TestsScriptsFileLocation}") {
                powershell(script: """
                    py ${TestsScriptsFileName}
                """)      
              }
         }
      }
      stage('Stop test app') {
         steps {
            powershell(script: """
               docker-compose down
            """)
         }
      }
      }
   }
}