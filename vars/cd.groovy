/*
	forma de invocación de método call:
	def ejecucion = load 'script.groovy'
	ejecucion.call()
*/
def call(stages, compileTool){
/*
    def stagesList = stages.split(";")
    sh "echo Stages: ${stagesList}"

    def listStagesOrder = [
        'build': 'sBuild',
        'sonar': 'sSonar',
        'run_spring_curl': 'sCurl',
        'upload_nexus': 'sNexusUpload',
        'download_nexus': 'sNexusDownload',
        'run_jar': 'sJar',
        'curl_jar': 'sTest'
    ]
*/
    //def arrayUtils = new array.arrayExtentions();
   // def stagesArray = []
        //stagesArray = arrayUtils.searchKeyInArray(stages, ";", listStagesOrder)

  /*  if (stagesArray.isEmpty()) {*/
  //      echo 'El pipeline se ejecutará completo'
        allStages2()
/*    } else {
        echo 'Stages a ejecutar :' + stages
        stagesArray.each{ stageFunction ->//variable as param
            echo 'Ejecutando ' + stageFunction
            "${stageFunction}"()
        }
    }*/

    

}

def allStages(){
        sBuild()
        sSonar()
        sCurl()
        sNexusUpload()
        sNexusDownload()
        sJar()
        sTest()
}

def allStages2(){
        def branch = env.GIT_BRANCH
        gitDiff(env.GIT_BRANCH)
        nexusDownload()
        sRun()
        test() 
        gitMergeMaster(env.GIT_BRANCH)
        gitMergeDevelop(env.GIT_BRANCH)
        gitTagMaster(branch.substring(9))
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


def sBuild() {
    stage("Paso 1: Build && Test"){
        env.TAREA = env.STAGE_NAME
        sh "gradle clean build"
    }

}
    
def sSonar() {
    stage("Paso 2: Sonar - Análisis Estático"){
        env.TAREA = env.STAGE_NAME
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh './gradlew sonarqube -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }

}

def sCurl() {
    stage("Paso 3: Curl Springboot Gradle sleep 20"){
        env.TAREA = env.STAGE_NAME
        sh "gradle bootRun&"
        sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }

}
    
def sNexusUpload() {
    stage("Paso 4: Subir Nexus"){
        env.TAREA = env.STAGE_NAME
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-usach-nexus',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: 'jar',
                    filePath: 'build/libs/DevOpsUsach2020-0.0.1.jar'
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

def sNexusDownload() {
    stage("Paso 5: Descargar Nexus"){
        env.TAREA = env.STAGE_NAME
        sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }
}
    
def sJar() {
    stage("Paso 6: Levantar Artefacto Jar"){
        env.TAREA = env.STAGE_NAME
        sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }
}
    
def sTest() {
    stage("Paso 7: Testear Artefacto - Dormir(Esperar 20sg) "){
        env.TAREA = env.STAGE_NAME
        sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}
return this;