pipeline {
    agent any
    options {
        disableConcurrentBuilds()
    }

    environment {
      // Apache Maven related side notes:
      // -B : recommended in CI to inform maven to not run in interactive mode (less logs)
      // -V : strongly recommended in CI, will display the JDK and Maven versions in use.
      //      Very useful to be quickly sure the selected versions were the ones you think.
      // -U : force maven to update snapshots each time (default : once an hour, makes no sense in CI).
      // -Dsurefire.useFile=false : useful in CI. Displays test errors in the logs directly (instead of
      //                            having to crawl the workspace files to see the cause).
      // -Dmaven.javadoc.skip=true : Skip javadoc as it is not used.
      MC_M2_OPTS="-B -V -U -Dsurefire.useFile=false -Dmaven.test.failure.ignore=true --fail-at-end -Dmaven.javadoc.skip=true"

      MC_APPLI = readMavenPom().getArtifactId()
    }
    tools {
        maven 'Maven'
        jdk 'JDK11'
    }
    stages {
        stage ('Determine build version') {
            steps {
                script {
                    //env.MC_APPLI = sh script: 'mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout', returnStdout: true
                    //define sane default
                    def mvnVersion = "0.0.0-undefined-SNAPSHOT"
                    def nexusIqStage = "develop"

                    if (env.TAG_NAME != null && env.TAG_NAME.length() > 0) {
                        def matcher = env.TAG_NAME =~ /^[a-zA-Z]+-(?<version>\d+\.\d+\.\d+(?<prerelease>-[a-zA-Z0-9-_.]+)?)$/
                        if (matcher.matches()) {
                            mvnVersion = matcher.group("version")
                            if (matcher.group("prerelease")) {
                                nexusIqStage = "stage-release"
                            } else {
                                nexusIqStage = "release"
                            }
                        } else {
                            error 'Le nom du tag n\'est pas valide'
                        }
                    } else if (env.BRANCH_NAME != null && env.BRANCH_NAME.length() > 0) {
                        if (env.BRANCH_NAME ==~ /^[a-z]+$/) {
                            if (env.BRANCH_NAME in ['master', 'main']) {
                                nexusIqStage = "build"
                            }
                            mvnVersion = "0.0.0-${env.BRANCH_NAME}-SNAPSHOT"
                        } else if (env.BRANCH_NAME ==~ /^v\d+\.\d+\.\d+(-[a-zA-Z0-9-_]+)?$/) {
                            mvnVersion= env.BRANCH_NAME.substring(1) + "-SNAPSHOT"
                         } else if (env.BRANCH_NAME ==~ /^\#\d+.*$/) {
                                def matcher = env.BRANCH_NAME =~ /^\#(?<redmine>\d+).*$/
                                if (matcher.matches()) {
                                    def redmine = matcher.group("redmine")
                                    mvnVersion = "0.0.0-${redmine}-SNAPSHOT"
                                }
                            }
                        }
                    env.MC_NEXUS_IQ_STAGE = nexusIqStage
                    env.MC_M2_OPTS_REVISION = "${env.MC_M2_OPTS} -Drevision=${mvnVersion}"
                }
            }
        }
        stage ('Build') {
            steps {
                sh 'env'
                sh 'mvn ${MC_M2_OPTS_REVISION} clean jacoco:prepare-agent install jacoco:report jacoco:report-aggregate org.cyclonedx:cyclonedx-maven-plugin:makeBom deploy'
            }
            post {
                success {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv(credentialsId: 'sonar-prod', installationName: 'sonar') {
                    sh 'mvn ${MC_M2_OPTS_REVISION} sonar:sonar'
                }
            }
        }
//         stage("SonarQube Quality Gate") {
//             steps {
//                 timeout(time: 5, unit: 'MINUTES') {
//                     // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
//                     // true = set pipeline to UNSTABLE, false = don't
//                     waitForQualityGate abortPipeline: true
//                 }
//             }
//         }
        stage('NexusIQ analysis') {
            parallel {
                stage('Analysing xaf-back') {
                    steps {
                        nexusPolicyEvaluation(
                        		iqApplication: "${MC_APPLI}-back",
                        		iqInstanceId: 'nexusiq',
                        		iqStage: "${MC_NEXUS_IQ_STAGE}",
                        		iqScanPatterns: [[scanPattern: "${MC_APPLI}-back/target/*.jar"],
                        		[scanPattern: "${MC_APPLI}-back/target/bom.xml"]]
                        )
                    }
                }
                stage('Analysing xaf-servlet') {
                    steps {
                        nexusPolicyEvaluation(
                        		iqApplication: "${MC_APPLI}-servlet",
                        		iqInstanceId: 'nexusiq',
                        		iqStage: "${MC_NEXUS_IQ_STAGE}",
                        		iqScanPatterns: [[scanPattern: "${MC_APPLI}-servlet/target/*.jar"],
                        		[scanPattern: "${MC_APPLI}-servlet/target/bom.xml"]]
                        )
                    }
                }
                stage('Analysing xaf-apiclient') {
                    steps {
                        nexusPolicyEvaluation(
                        		iqApplication: "${MC_APPLI}-apiclient",
                        		iqInstanceId: 'nexusiq',
                        		iqStage: "${MC_NEXUS_IQ_STAGE}",
                        		iqScanPatterns: [[scanPattern: "${MC_APPLI}-apiclient/target/*.jar"],
                        		[scanPattern: "${MC_APPLI}-apiclient/target/bom.xml"]]
                        )
                    }
                }
            }
        }
    }
}