pipeline {
  agent any
  tools { 
    maven 'Maven 3.6.1' 
  }
  stages {
    stage('Initialize') {
      steps {
        sh '''
          echo "$PATH"
          java -version
          echo $JAVA_VERSION
          mvn -version
          '''
      }
    }
    stage('Build') {
      steps {
        sh 'mvn clean install jacoco:report'
      }
    }
    stage('Sonar') {
      steps {
        sh 'mvn sonar:sonar -Dsonar.host.url=https://sonar.mlobb.sk'
      }
    }
  }
}
