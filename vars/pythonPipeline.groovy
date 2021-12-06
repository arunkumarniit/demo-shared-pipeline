def call (body) {
   def config = [:]
   body.resolveStrategy = Closure.DELEGATE_FIRST
   body.delegate = config
   body()

   String ChangeLog = '';
   String RepoUrl = config.repoUrl;
   String RepoBranch = config.RepoBranch;

   pipeline {
      agent any
      stages {
         stage('Checkout') {
            steps {
              
              git branch: "${RepoBranch}", url: "${RepoUrl}" 
              
            }
         }
      }
   }
}