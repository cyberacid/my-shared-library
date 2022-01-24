// vars/evenOrOdd.groovy
def call(int buildNumber) {
  if (buildNumber % 2 == 0) {
    pipeline {
        agent any
        
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
            }
        }
    }
  } else {
    pipeline {
      agent any
      stages {
        stage('Odd Stage') {
          steps {
            echo "The build number is odd"
          }
        }
      }
    }
  }
}