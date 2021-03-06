#!/usr/bin/env groovy

def call(Closure body={}) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        agent none

        options {
            skipDefaultCheckout()
        }

        triggers {
            pollSCM('H * * * *')
        }

        environment {
            LANG = "C.UTF-8"
            LC_ALL = "en_US.UTF-8"
            LANGUAGE = "en_US.UTF-8"
            UNITTESTING_STATE = 'false'
            TESTING_STATE = 'false'
            App = "HelloTalk"
            CHINAPRODUCTFLAVORS_STATE = 'true'
            GOOGLEPRODUCTFLAVORS_STATE = 'true'
            HTPRIVATEPRODUCTFLAVORS_STATE = 'false'
            buildTypes = "release"
        }

        stages {
            stage('Check Branch/Tag') {
                agent {
                    node {
                        label 'master'
                        customWorkspace "workspace/${JOB_NAME}"
                    }
                }

                when {
                    beforeAgent true
                    not {
                        anyOf {
                            branch "develop"
                        }
                    }
                }

                steps {
                    error "Don't know what to do with this branch or tag: ${env.BRANCH_NAME}"
                }
            }

            stage('Start') {
                when {
                    beforeAgent true
                    branch "develop"
                }

                failFast false

                parallel {
                    stage('Start - china flavor') {
                        environment {
                            productFlavors = "china"
                        }

                        when {
                            beforeAgent true
                            environment name: 'CHINAPRODUCTFLAVORS_STATE', value: 'true'
                        }

                        stages {
                            stage('Checkout SCM - china flavor') {
                                agent {
                                    node {
                                        label 'mac-mini1'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                steps {
                                    script {
                                        def scmVars = checkoutGithub()
                                        def branchCode = gitVersioner.branchCode()
                                        def news = readFile file: "NEWS.md", encoding: "UTF-8"
                                        env.changelogChina = "---[${productFlavors}]+" + branchCode + news
                                    }
                                }
                            }

                            stage('Prepare - china flavor') {
                                agent {
                                    node {
                                        label 'mac-mini1'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                environment {
                                    ANDROID_SDK_ROOT = "${HOME}/Library/Android/sdk"
                                    ANDROID_HOME = "${ANDROID_SDK_ROOT}"
                                }

                                steps {
                                    script {
                                        gradle.version()
                                    }
                                }
                            }

                            stage('clean - china flavor') {
                                agent {
                                    node {
                                        label 'mac-mini1'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                environment {
                                    ANDROID_SDK_ROOT = "${HOME}/Library/Android/sdk"
                                    ANDROID_HOME = "${ANDROID_SDK_ROOT}"
                                }

                                steps {
                                    script {
                                        gradle.clean()
                                    }
                                }
                            }

                            stage('Unit Testing - china flavor') {
                                agent {
                                    node {
                                        label 'mac-mini1'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                environment {
                                    ANDROID_SDK_ROOT = "${HOME}/Library/Android/sdk"
                                    ANDROID_HOME = "${ANDROID_SDK_ROOT}"
                                }

                                when {
                                    beforeAgent true
                                    environment name: 'UNITTESTING_STATE', value: 'true'
                                }

                                steps {
                                    unittestDevelopBranch(buildTypes, productFlavors)
                                }
                            }

                            stage('Build - china flavor') {
                                agent {
                                    node {
                                        label 'mac-mini1'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                environment {
                                    ANDROID_SDK_ROOT = "${HOME}/Library/Android/sdk"
                                    ANDROID_HOME = "${ANDROID_SDK_ROOT}"
                                }

                                steps {
                                    buildDevelopBranch(buildTypes, productFlavors)
                                    androidFirPublish("${WORKSPACE}/HelloTalk/build/outputs/apk/china/release/HelloTalk-china-release.apk", env.changelogChina)
                                }
                            }

                            /*stage('Artifacts - china flavor') {
                                agent {
                                    node {
                                        label 'mac-mini1'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                steps {
                                    artifactsDevelopBranch(buildTypes, productFlavors)
                                }
                            }

                            stage('Deploy - china flavor') {
                                agent {
                                    node {
                                        label 'master'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                steps {
                                    deployDevelopBranch(buildTypes, productFlavors)
                                }
                            }

                            stage('Testing - china flavor') {
                                agent {
                                    node {
                                        label 'mac-mini1'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                when {
                                    beforeAgent true
                                    environment name: 'TESTING_STATE', value: 'true'
                                }

                                steps {
                                    echo "Test"
                                }
                            }*/
                        }
                    }

                    stage('Start - google flavor') {
                        environment {
                            productFlavors = "google"
                        }

                        when {
                            beforeAgent true
                            environment name: 'GOOGLEPRODUCTFLAVORS_STATE', value: 'true'
                        }

                        stages {
                            stage('Checkout SCM - google flavor') {
                                agent {
                                    node {
                                        label 'mac-mini2'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                steps {
                                    script {
                                        def scmVars = checkoutGithub()
                                        def branchCode = gitVersioner.branchCode()
                                        def news = readFile file: "NEWS.md", encoding: "UTF-8"
                                        env.changelogGoogle = "---[${productFlavors}]+" + branchCode + news
                                    }
                                }
                            }

                            stage('Prepare - google flavor') {
                                agent {
                                    node {
                                        label 'mac-mini2'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                environment {
                                    ANDROID_SDK_ROOT = "${HOME}/Library/Android/sdk"
                                    ANDROID_HOME = "${ANDROID_SDK_ROOT}"
                                }

                                steps {
                                    script {
                                        gradle.version()
                                    }
                                }
                            }

                            stage('clean - google flavor') {
                                agent {
                                    node {
                                        label 'mac-mini2'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                environment {
                                    ANDROID_SDK_ROOT = "${HOME}/Library/Android/sdk"
                                    ANDROID_HOME = "${ANDROID_SDK_ROOT}"
                                }

                                steps {
                                    script {
                                        gradle.clean()
                                    }
                                }
                            }
                            stage('Unit Testing - google flavor') {
                                agent {
                                    node {
                                        label 'mac-mini2'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                environment {
                                    ANDROID_SDK_ROOT = "${HOME}/Library/Android/sdk"
                                    ANDROID_HOME = "${ANDROID_SDK_ROOT}"
                                }

                                when {
                                    beforeAgent true
                                    environment name: 'UNITTESTING_STATE', value: 'true'
                                }

                                steps {
                                    unittestDevelopBranch(buildTypes, productFlavors)
                                }
                            }

                            stage('Build - google flavor') {
                                agent {
                                    node {
                                        label 'mac-mini2'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                environment {
                                    ANDROID_SDK_ROOT = "${HOME}/Library/Android/sdk"
                                    ANDROID_HOME = "${ANDROID_SDK_ROOT}"
                                }

                                steps {
                                    buildDevelopBranch(buildTypes, productFlavors)
                                    androidFirPublish("${WORKSPACE}/HelloTalk/build/outputs/apk/google/release/HelloTalk-google-release.apk", env.changelogGoogle)
                                }
                            }

                            /*stage('Artifacts - google flavor') {
                                agent {
                                    node {
                                        label 'mac-mini2'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                steps {
                                    artifactsDevelopBranch(buildTypes, productFlavors)
                                }
                            }

                            stage('Deploy - google flavor') {
                                agent {
                                    node {
                                        label 'master'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                steps {
                                    deployDevelopBranch(buildTypes, productFlavors)
                                }
                            }

                            stage('Testing - google flavor') {
                                agent {
                                    node {
                                        label 'mac-mini2'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }

                                when {
                                    beforeAgent true
                                    environment name: 'TESTING_STATE', value: 'true'
                                }

                                steps {
                                    echo "Test"
                                }
                            }*/
                        }
                    }
                }
            }
        }
    }
}

def unittestDevelopBranch(String buildTypes='', String productFlavors='') {
    echo "Develop branch - Unit Testing"
    buildTypes = pipelineAndroidAppSetup.changeStringGradleStyle(buildTypes)
    productFlavors = pipelineAndroidAppSetup.changeStringGradleStyle(productFlavors)
    def args = ((productFlavors ?: '') + (buildTypes ?: '')) ? (((productFlavors ?: '') + (buildTypes ?: '')) + 'UnitTest' ) : ''
    pipelineAndroidAppSetup.unittest(args)
}

def buildDevelopBranch(String buildTypes='', String productFlavors='') {
    echo "Develop branch - Build"
    buildTypes = pipelineAndroidAppSetup.changeStringGradleStyle(buildTypes)
    productFlavors = pipelineAndroidAppSetup.changeStringGradleStyle(productFlavors)
    def args = ((productFlavors ?: '') + (buildTypes ?: '')) //+ " publish"
    pipelineAndroidAppSetup.build(args)
}

def artifactsDevelopBranch(String buildTypes = '', String productFlavors = '') {
    echo "Develop branch - Artifacts"
    def name = "${App}" + (((productFlavors ? ('-' + productFlavors) : '') + (buildTypes ? ('-'+ buildTypes) : '')) ?: '')
    def path = "${App}/build/outputs/apk/" + (productFlavors ?: '*') + '/' + (buildTypes ?: '*') + "/${App}-" + (productFlavors ?: '*') + '-' + (buildTypes ?: '*') + '.apk'
    pipelineAndroidAppSetup.artifacts(name, path)
}

def deployDevelopBranch(String buildTypes = '', String productFlavors = '') {
    echo "Develop branch - Deploy"
    def name = "${App}" + (((productFlavors ? ('-' + productFlavors) : '') + (buildTypes ? ('-'+ buildTypes) : '')) ?: '')
    def path = "${App}/build/outputs/apk/" + (productFlavors ?: '*') + '/' + (buildTypes ?: '*') + "/${App}-" + (productFlavors ?: '*') + '-' + (buildTypes ?: '*') + '.apk'
    def targetPath = "/var/www/nginx/html/testing.hellotalk.com/android/package/"
    pipelineAndroidAppSetup.deploy(name, path, targetPath)
}