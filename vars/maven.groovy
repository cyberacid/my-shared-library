/*
	forma de invocación de método call:
	def ejecucion = load 'script.groovy'
	ejecucion.call()
*/
def call(stages){
  
    def branch = env.GIT_BRANCH;

  if (branch.startsWith('feature-') || branch == 'develop') {
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


def gitDiff(branch) {
stage("Paso 7: Git Diff"){
      env.TAREA = env.STAGE_NAME
      sh "DIFERENCIAS ${branch} VS MAIN:"
      sh "git diff ${branch}..main"
  }

}

def nexusDownload() {
stage("Paso 8: Descargar Nexus"){
      env.TAREA = env.STAGE_NAME
      sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
  }

}

def sRun() {

  stage("Paso 9: Levantar Artefacto Jar"){
      env.TAREA = env.STAGE_NAME
      sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
  }

}

def test() {

  stage("Paso 10: Testear Artefacto - Dormir(Esperar 20sg) "){
      env.TAREA = env.STAGE_NAME
      sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
  }

}

def gitMergeMaster(branch) {

stage("Paso 11: Git Merge Master"){
      env.TAREA = env.STAGE_NAME
      sh "git checkout main && git pull origin main"
      sh "git merge --no-ff ${branch}"
      sh "git push origin main"
  }
}

def gitMergeDevelop(branch) {
stage("Paso 12: Git Merge Develop"){
      env.TAREA = env.STAGE_NAME
      sh "git checkout main && git pull origin main"
      sh "git merge --no-ff ${branch}"
      sh "git push origin main"
  }

}

def gitTagMaster(tag) {

stage("Paso 13: Git Merge Develop"){
      env.TAREA = env.STAGE_NAME
      sh "git checkout main && git pull origin main"
      sh "git tag ${tag}"
      sh "git push origin ${tag}"
  }
}

return this;