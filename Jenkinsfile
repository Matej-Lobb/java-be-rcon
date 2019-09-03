pipeline {
  agent any
  stages {
    stage('Initialize') {
      steps {
        sh '''
          export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
          exoirt PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/lib/jvm/java-11-openjdk/bin
          echo "$PATH"
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
