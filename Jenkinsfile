pipeline {
  agent any
  stages {
    stage('Initialize') {
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
    stage('Codacy') {
      steps {
        sh '''
        wget -O codacy-coverage-reporter https://github.com/codacy/codacy-coverage-reporter/releases/download/6.0.4/codacy-coverage-reporter-linux-6.0.4
        chmod +x codacy-coverage-reporter
        ./codacy-coverage-reporter report -l Java -r target/site/jacoco/jacoco.xml
        '''
      }
    }
    stage('Codecov') {
      steps {
        sh 'curl -s https://codecov.io/bash | bash -s'
      }
    }
  }
  tools {
    maven 'Maven 3.6.1'
  }
  environment {
    CODACY_PROJECT_TOKEN = '231d4bacc62d4b8da5e4b45526b5e0e7'
    CODECOV_TOKEN = '76a68c87-a2e7-4cc9-937f-8b28459ae43c'
  }
}
