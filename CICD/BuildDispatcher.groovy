try{
        checkout scm
        load 'CICD/Build_Jenkinsfile'

} catch (Exception e){
        ansiColor{
            print (\'\'\'
            This is only default script. That means you're branch has no correct            
            jenkins file for this job. If any customization is needed fix this.             
            \'\'\')
        }
} finally {
        configFileProvider([configFile(fileId: 'buildDefault', variable: 'buildDefault')]) {
            load buildDefault
        }
}

//https://github.com/jenkinsci/pipeline-multibranch-defaults-plugin/blob/master/README.md