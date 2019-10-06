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
    stage('Codacy') {
      steps {
        sh '''
        wget -O codacy-coverage-reporter https://github.com/codacy/codacy-coverage-reporter/releases/download/6.0.4/codacy-coverage-reporter-linux-6.0.4
        chmod +x codacy-coverage-reporter
        ./codacy-coverage-reporter report -l Java -r dayz-server-manager-boot/target/site/jacoco-aggregate/jacoco.xml
        '''
      }
    }
  }
}
