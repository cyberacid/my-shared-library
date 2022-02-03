/*
	forma de invocación de método call:
	def ejecucion = load 'script.groovy'
	ejecucion.call()
*/
def call(stages, compileTool){
  

      compile()
      unitTest()
        jar()
        //sonar()
        //nexusUpload()
        gitCreateRelease() 
  }

}

def compile(){
    stage("Paso 1: Compliar"){
      env.TAREA = env.STAGE_NAME
      sh "mvn clean compile -e"
  }

}

def unitTest() {
stage("Paso 2: Testear"){
      env.TAREA = env.STAGE_NAME
      sh "mvn clean test -e"
  }

}

def jar() {
stage("Paso 3: Build .Jar"){
      env.TAREA = env.STAGE_NAME
      sh "mvn clean package -e"
  }

}

def sonar() {
stage("Paso 4: Sonar - Análisis Estático"){
      env.TAREA = env.STAGE_NAME
      sh "echo 'Análisis Estático!'"
      withSonarQubeEnv('sonarqube') {
          sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
      }
  }

}

def nexusUpload() {
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

}

def gitCreateRelease() {
stage("Paso 6: Release"){
      env.TAREA = env.STAGE_NAME
      sh "git checkout develop && git pull origin develop"
      sh "git branch -D release-v1.0.0"
      sh "git checkout -b release-v1.0.0"
      sh "git push origin release-v1.0.0"
  }

}



return this;