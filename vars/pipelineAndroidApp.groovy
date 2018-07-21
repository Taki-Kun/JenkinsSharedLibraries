#!/usr/bin/env groovy

def call(Closure body={}) {

    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        agent {
            node {
                label 'mac-mini'
                customWorkspace "workspace/${JOB_NAME}"
            }
        }
        environment {
            ANDROID_SDK_ROOT = "${HOME}/Library/Android/sdk"
            ANDROID_HOME = "${ANDROID_SDK_ROOT}"
            UNITTESTING = false
            ReleaseBuildTypes = "Release"
            ReleaseProductFlavors = "china"
            //-PBUILD_NUMBER=${env.BUILD_NUMBER}
        }
        stages {
            stage('Branch and Tag - error') {
                when {
                    not {
                        anyOf {
                            branch "feature/*"
                            branch "develop"
                            //branch "test"
                            branch "release/*"
                            branch "master"
                            branch "hotfix/*"
                            buildingTag()
                        }
                    }
                }
                steps {
                    error "Don't know what to do with this branch or tag: ${env.BRANCH_NAME}"
                }
            }
            stage('Checkout') {
                steps {
                    script {
                        def environment = new io.issenn.devops.jenkins.pipeline.environment.EnvironmentConstants(this)
                        //println(environment.repoName(this))
                        println(environment.BRANCH_NAME)
                        println(environment.JOB_NAME)
                    }

                    checkoutGitlab()
                }
            }
            stage('Prepare') {
                steps {
                    script {
                        //gradle '-v'
                        gradle.version()
                    }
                }
            }
            stage('Build snapshot - feature/*') {
                when {
                    branch "feature/*"
                }
                steps {
                    buildFeatureBranch()
                }
            }
            stage('Build snapshot - develop') {
                when {
                    branch "develop"
                }
                failFast false
                parallel {
                    stage('china flavor - develop') {
                        stages {
                            stage('Unit Testing') {
                                when {
                                    environment name: 'UNITTESTING', value: true
                                }
                                steps {
                                    unittestFeatureBranch()
                                }
                            }
                            stage('Build') {
                                steps {
                                    buildDevelopBranch(ReleaseBuildTypes, ReleaseProductFlavors)
                                }
                            }
                            stage('artifacts') {
                                steps {
                                    echo "artifacts"
                                }
                            }
                            stage('Deploy snapshot - develop') {
                                agent {
                                    label 'mac-mini'
                                }
                                steps {
                                    deployDevelopBranch()
                                }
                            }
                            stage('Test') {
                                steps {
                                    echo "Test"
                                }
                            }
                        }
                    }/*
                    stage('google flavor - feature/*') {
                        stages {
                            stage('') {
                                steps {
                                    buildDevelopBranch()
                                }
                            }
                        }
                    }*/
                }
            }

            stage('Build snapshot - release/*') {
                when {
                    branch "release/*"
                }
                steps {
                    buildReleaseBranch()
                }
            }

            stage('Build @ Prod - master') {
                when {
                    branch "master"
                }
                steps {
                    buildMasterBranch()
                }
            }

            stage('Build snapshot - hotfix/*') {
                when {
                    branch "hotfix/*"
                }
                steps {
                    buildHotfixBranch()
                }
            }
            // artifacts
            stage('artifacts') {
                steps {
                    echo "artifacts"
                }
            }
            // Deploy
            stage('Deploy snapshot - feature/*') {
                when {
                    branch "feature/*"
                }
                steps {
                    deployFeatureBranch()
                }
            }

            stage('Deploy snapshot - develop') {
                when {
                    branch "develop"
                }
                steps {
                    deployDevelopBranch()
                }
            }

            stage('Deploy snapshot - release/*') {
                when {
                    branch "release/*"
                }
                steps {
                    deployReleaseBranch()
                }
            }

            stage('Deploy @ Prod - master') {
                when {
                    branch "master"
                }
                steps {
                    deployMasterBranch()
                }
            }

            stage('Deploy snapshot - hotfix/*') {
                when {
                    branch "hotfix/*"
                }
                steps {
                    deployHotfixBranch()
                }
            }

            stage('Test') {
                steps {
                    echo "Test"
                }
            }

        }
    }
}

/**
 * feature/* for feature branches; merge back into develop
 * develop for ongoing development work
 * test/*
 * release/* to prepare production releases; merge back into develop and tag master
 * master for production-ready releases
 * hotfix/* to patch master quickly; merge back into develop and tag master
 */

def defaultBuildTypes = 'DailyBuild'

def unittestFeatureBranch() {
    echo "Feature branch - Unit Testing"
    //unittest(buildTypes, flavor)
}

def buildFeatureBranch() {
    echo "Feature branch"
}

def buildDevelopBranch(String buildTypes='', String flavor='') {
    echo "Develop branch"
    buildTypes = buildTypes ?: defaultBuildTypes
    flavor = flavor
    // test(buildTypes, flavor)
    build(buildTypes, flavor)
    // sonar()
    // javadoc()
    // deploy(env.JBOSS_TST)
}

def buildReleaseBranch() {
    echo "Release branch"
}

def buildMasterBranch() {
    echo "Master branch"
}

def buildHotfixBranch() {
    echo "Hotfix branch"
}

def deployFeatureBranch() {
    echo "Feature branch"
}

def deployDevelopBranch() {
    echo "Develop branch"
}

def deployReleaseBranch() {
    echo "Feature branch"
}

def deployMasterBranch() {
    echo "Feature branch"
}

def deployHotfixBranch() {
    echo "Feature branch"
}

def test(String buildTypes='', String flavor='') {
    echo "test"
    gradle "clean test"
}

def build(String buildTypes='', String flavor='') {
    echo "build"
    gradle "clean assemble${flavor}${buildTypes}"
}

def deploy() {
    echo "deploy"
}