// vars/ejecucion.groovy
def call() {
    pipeline {
        agent any
        triggers {
            GenericTrigger(
             genericVariables: [
              [key: 'ref', value: '$.ref']
             ],
                

             
                genericRequestVariables: [
                [key: 'compileTool', regexpFilter: ''], 
                [key: 'stages', regexpFilter: '']
            ],
                causeString: 'Triggered on $compileTool',
             token: 'abc123',
             tokenCredentialId: '',

             printContributedVariables: true,
             printPostContent: true,

             silentResponse: false,

             regexpFilterText: '$ref',
             regexpFilterExpression: 'refs/heads/' + BRANCH_NAME
            )
        }
        
       
        
        environment {
            NEXUS_USER = credentials('user-nexus')
            NEXUS_PASSWORD    = credentials('password-nexus')
            GITHUB_TOKEN = credentials('github-cyber')
        }
        
        stages {

            stage("Paso 00: Select Compile Tool") {
                steps {
                    script{
                        env.TAREA = ""
                        GIT_REPO = env.GIT_URL.replaceFirst(/^.*\/([^\/]+?).git$/, '$1')
                        sh "echo ' PROJECT SONAR ${GIT_REPO}-${env.GIT_BRANCH}-${env.BUILD_NUMBER}'"
                        sh "echo 'STAGES: ${env.stages} compileTool: ${env.compileTool}'"
                        if (fileExists('build.gradle')) {
                            sh "echo 'App Gradle'"
                            //gradle.call(env.stages, env.compileTool)
                        } else if(fileExists('pom.xml'))  {
                            sh "echo 'App Maven'"
                            //maven.call(env.stages, env.compileTool)
                        } else {
                            sh "echo 'App sin identificar'"
                        }

                            def branch = env.GIT_BRANCH;

                            if (branch.startsWith('feature-') || branch == 'develop') {
                                ci.call(env.stages, env.compileTool)
                            } else if (branch.startWith('release-v')) {
                                cd.call(env.stages, env.compileTool)
                            }

                    }
                }
            }
  
        }
        post {
            always {
                sh "echo 'fase always executed post'"
            }
            success {
                sh "echo 'fase success'"
                slackSend channel: "#lab-pipeline-mod3-seccion3-status", message: "[Grupo3][Pipeline IC][Rama: ${env.GIT_BRANCH}][Stage: build][Resultado: Ok]"
            }

            failure {
                sh "echo 'fase failure'"
            }
        }
    }
}
