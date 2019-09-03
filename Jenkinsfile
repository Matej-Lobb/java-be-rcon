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
