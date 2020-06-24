pipeline {
    agent {
        label 'lsf-submitter'
    }
    environment {
        JAVA_HOME = '/nfs/ma/home/java/jdk-11.0.2'
    }
    stages {
        stage('Build') {
            steps {
                sh 'git submodule update --init --recursive'
            }
        }
        stage('Test') {
            steps {
                sh 'PATH=$JAVA_HOME/bin:$PATH ./gradlew -Dfile.encoding=UTF-8 -PbuildProfile=integration -PtestResultsPath=ut test --tests *Test'
                sh 'PATH=$JAVA_HOME/bin:$PATH ./gradlew -Dfile.encoding=UTF-8 -PbuildProfile=integration -PtestResultsPath=it -PexcludeTests=**/*WIT.class test --tests *IT'
                junit 'build/test-results/**/*.xml'
            }
        }
    }
}
