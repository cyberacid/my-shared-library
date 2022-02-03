/*
	forma de invocación de método call:
	def ejecucion = load 'script.groovy'
	ejecucion.call()
*/
def call(stages){
  stage("Paso 1: Compliar"){
      env.TAREA = env.STAGE_NAME
      sh "mvn clean compile -e"
  }
  stage("Paso 2: Testear"){
      env.TAREA = env.STAGE_NAME
      sh "mvn clean test -e"
  }
  stage("Paso 3: Build .Jar"){
      env.TAREA = env.STAGE_NAME
      sh "mvn clean package -e"
  }
  /*
  stage("Paso 4: Sonar - Análisis Estático"){
      env.TAREA = env.STAGE_NAME
      sh "echo 'Análisis Estático!'"
      withSonarQubeEnv('sonarqube') {
          sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
      }
  }
  stage("Paso 5: Curl Springboot Gradle sleep 20"){
      env.TAREA = env.STAGE_NAME
      sh "gradle bootRun&"
      sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
  }
  stage("Paso 6: Subir Nexus"){
      env.TAREA = env.STAGE_NAME
      nexusPublisher nexusInstanceId: 'nexus',
      nexusRepositoryId: 'devops-usach-nexus',
      packages: [
          [$class: 'MavenPackage',
              mavenAssetList: [
                  [classifier: '',
                  extension: 'jar',
                  filePath: 'build/DevOpsUsach2020-0.0.1.jar'
              ]
          ],
              mavenCoordinate: [
                  artifactId: 'DevOpsUsach2020',
                  groupId: 'com.devopsusach2020',
                  packaging: 'jar',
                  version: '0.0.1'
              ]
          ]
      ]
  }
  stage("Paso 7: Descargar Nexus"){
      env.TAREA = env.STAGE_NAME
      sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
  }
  stage("Paso 8: Levantar Artefacto Jar"){
      env.TAREA = env.STAGE_NAME
      sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
  }
  stage("Paso 9: Testear Artefacto - Dormir(Esperar 20sg) "){
      env.TAREA = env.STAGE_NAME
      sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
  }
  */
}
return this;