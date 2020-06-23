pipeline {
    agent 'lsf-submitter'
    stages {
        stage('Build') {
            steps {
                //
            }
        }
        stage('Test') {
            steps {
                sh 'export JAVA_HOME=/nfs/ma/home/java/jdk-11.0.2'
                sh 'export PATH=$JAVA_HOME/bin:$PATH'
                sh './gradlew -Dfile.encoding=UTF-8 -PbuildProfile=integration -PtestResultsPath=ut test --tests *Test'
                sh './gradlew -Dfile.encoding=UTF-8 -PbuildProfile=integration -PtestResultsPath=it -PexcludeTests=**/*WIT.class test --tests *IT'
                junit 'build/test-results/**/*.xml'
            }
        }
    }
}
