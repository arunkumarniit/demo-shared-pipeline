def call (body) {
   def config = [:]
   body.resolveStrategy = Closure.DELEGATE_FIRST
   body.delegate = config
   body();

   String ChangeLog = '';
   String RepoUrl = config.repoUrl;
   String RepoBranch = config.repoBranch;

   pipeline {
      agent any
      stages {
         stage('Checkout') {
            steps {
                echo "Workspace is ${pwd()}";
                git branch: "${RepoBranch}", url: "${RepoUrl}" 
              
            }
         }
      }
   }
}