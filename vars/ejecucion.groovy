// vars/ejecucion.groovy
def call() {
    pipeline {
        agent any
        triggers {
            GenericTrigger(
             genericVariables: [
              [key: 'ref', value: '$.ref']
             ],
                

             causeString: 'Triggered on $ref',
                genericRequestVariables: [
                [key: 'compileTool', regexpFilter: ''], 
                [key: 'stages', regexpFilter: '']
            ],

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
        }
        
        stages {
            stage("Paso 0: Download and checkout"){
                steps {
                    
                    
                    checkout(
                        [$class: 'GitSCM',
                        //Acá reemplazar por el nonbre de branch
                        branches: [[name: "sonarqube" ]],
                        //Acá reemplazar por su propio repositorio
                        userRemoteConfigs: [[url: 'https://github.com/cyberacid/ejemplo-maven.git']]])
                        sh "echo ${env.GIT_BRANCH}"
                        sh 'printenv'
                }
            }
            stage("Paso 00: Select Compile Tool") {
                steps {
                    script{
                        env.TAREA = ""
                        switch($compileTool)
                        {
                            case 'Maven':
                                //def ejecucion = load 'maven.groovy'
                                //maven.call(params.stages)
                            sh "echo MAVEN"
                            break;
                            case 'Gradle':
                                //def ejecucion = load 'gradle.groovy'
                                //gradle.call(params.stages)
                            sh "echo GRADLE"
                            break;
                        }
                    }
                }
            }
            stage("Paso 1: Compliar"){
                steps {
                    script {
                    sh "echo 'Compile Code!'"
                    // Run Maven on a Unix agent.
                    sh "mvn clean compile -e"
                    }
                }
            }
            stage("Paso 2: Testear"){
                steps {
                    script {
                    sh "echo 'Test Code!'"
                    // Run Maven on a Unix agent.
                    sh "mvn clean test -e"
                    }
                }
            }
            stage("Paso 3: Build .Jar"){
                steps {
                    script {
                    sh "echo 'Build .Jar!'"
                    // Run Maven on a Unix agent.
                    sh "mvn clean package -e"
                    }
                }
            }
            /*
            stage("Paso 4: Análisis SonarQube"){
                steps {
                    withSonarQubeEnv('sonarqube') {
                        sh "echo 'Calling sonar Service in another docker container!'"
                        // Run Maven on a Unix agent to execute Sonar.
                        sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=github-sonar'
                    }
                }
                post {
                    //record the test results and archive the jar file.
                    success {
                        //archiveArtifacts artifacts:'build/*.jar'
                        nexusPublisher nexusInstanceId: 'nexus',
                            nexusRepositoryId: 'devops-usach-nexus',
                            packages: [
                                [$class: 'MavenPackage',
                                    mavenAssetList: [
                                        [classifier: '',
                                        extension: 'jar',
                                        filePath: 'build/DevOpsUsach2020-0.0.1.jar']
                                    ],
                            mavenCoordinate: [
                                artifactId: 'DevOpsUsach2020',
                                groupId: 'com.devopsusach2020',
                                packaging: 'jar',
                                version: '0.0.7']
                            ]
                        ]
                    }
                }
            }
            stage('Paso 5: Bajar Nexus Stage') {
                steps {
                    sh "curl -X GET -u ${NEXUS_USER}:${NEXUS_PASSWORD} http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.7/DevOpsUsach2020-0.0.7.jar -O"
                    sh "ls"
                }
            }
            stage("Paso 6: Levantar Springboot APP"){
                steps {
                    sh 'nohup bash java -jar DevOpsUsach2020-0.0.7.jar & >/dev/null'
                }
            }
            stage("Paso 7: Curl con Sleep de prueba "){
                steps {
                sh "sleep 20 && curl -X GET 'http://localhost:8080/rest/mscovid/test?msg=testing'"
                }
            }
            stage("Paso 8: Subir nueva Version"){
                steps {
                    //archiveArtifacts artifacts:'build/*.jar'
                    nexusPublisher nexusInstanceId: 'nexus',
                        nexusRepositoryId: 'devops-usach-nexus',
                        packages: [
                            [$class: 'MavenPackage',
                                mavenAssetList: [
                                    [classifier: '',
                                    extension: 'jar',
                                    filePath: 'DevOpsUsach2020-0.0.7.jar']
                                ],
                        mavenCoordinate: [
                            artifactId: 'DevOpsUsach2020',
                            groupId: 'com.devopsusach2020',
                            packaging: 'jar',
                            version: '1.0.0']
                        ]
                    ]
                }
            }*/
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
