pipeline {
  agent any
  stages {
    stage('Initialize') {
      steps {
        sh '''
          export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
          echo "$PATH"
          echo "$M2_HOME"
          echo "$JAVA_HOME"
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
