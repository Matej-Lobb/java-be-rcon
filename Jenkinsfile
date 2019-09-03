pipeline {
  agent any
  tools {
    jdk 'jdk11'
  }
  stages {
    stage('Initialize') {
      steps {
        sh '''
          echo "PATH = ${PATH}"
          echo "M2_HOME = ${M2_HOME}"
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
