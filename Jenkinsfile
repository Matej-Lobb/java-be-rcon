pipeline {
  agent any
  stages {
    stage('Initialize') {
      steps {
        sh '''
          echo "$PATH"
          mvn -version
          java -version
          '''
      }
    }
    stage('Build') {
      steps {
        sh 'mvn clean install'
      }
    }
    stage('Sonar') {
      steps {
        sh 'mvn sonar:sonar -Pcoverage -Dsonar.host.url=http://116.203.153.168:8500'
      }
    }
  }
}
