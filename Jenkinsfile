pipeline {
  agent any
  tools { 
    maven 'Maven 3.6.1' 
  }
  stages {
    stage('Pipeline Initialize') {
      steps {
        sh '''
          java -version
          echo $JAVA_VERSION
          mvn -version
          echo $M2_HOME
          echo $MAVEN_HOME
          echo "$PATH"
          '''
      }
    }
    stage('Build and Test') {
      steps {
        sh 'mvn clean compile'
        sh 'mvn install jacoco:report'

      }
    }
    stage('Sonar') {
      steps {
        sh 'mvn sonar:sonar -Dsonar.host.url=https://sonar.mlobb.sk'
      }
    }
  }
}
