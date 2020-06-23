pipeline {
    agent {
        label 'lsf-submitter'
    }
    tools {
        jdk 'openjdk-11'
    }
    environment {
        JAVA_HOME = '/nfs/ma/home/java/jdk-11.0.2'
        PATH = '$JAVA_HOME/bin:$PATH'
    }
    stages {
//         stage('Build') {
//             steps {
//                 //
//             }
//         }
        stage('Test') {
            steps {
                sh './gradlew -Dfile.encoding=UTF-8 -PbuildProfile=integration -PtestResultsPath=ut test --tests *Test'
                sh './gradlew -Dfile.encoding=UTF-8 -PbuildProfile=integration -PtestResultsPath=it -PexcludeTests=**/*WIT.class test --tests *IT'
                junit 'build/test-results/**/*.xml'
            }
        }
    }
}
