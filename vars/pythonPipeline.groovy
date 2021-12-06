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
                echo "Workspace is ${pwd()}";
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
      }
   }
}