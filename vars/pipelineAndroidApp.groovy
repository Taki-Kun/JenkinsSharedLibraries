#!/usr/bin/env groovy

def call(Closure body={}) {

    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        agent {
            label 'mac-mini'
        }

        environment {
            ANDROID_SDK_ROOT="${HOME}/Library/Android/sdk"
            ANDROID_HOME="${ANDROID_SDK_ROOT}"
        }

        stages {
            stage('Checkout') {
                steps {
                    checkoutGitlab()
                }
            }

            stage('Prepare') {
                steps {
                    script {
                        //sh(returnStdout: true, script: "ls")
                        def z = new io.issenn.devops.jenkins.pipeline.gradle.GradleConstants(this)
                        println(z.gradle '-v')
                    }
                }
            }

        }
    }
}