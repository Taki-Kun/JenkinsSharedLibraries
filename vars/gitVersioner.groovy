#!/usr/bin/env groovy

import java.io.File

def call() {

    // check if git project
    def status = 'git status'.execute()
    status.waitFor()
    def isGitProject = status.exitValue()
}

def tag() {
    def currentTag = ""
    // def String path = WORKSPACE
    def path = "/Users/mac/Library/Jenkins-slave1/workspace/AndroidJekins"
    println(path)
    def file = new File(path)
    def ls = "ls".execute([], file)
    ls.waitFor()
    println(ls.exitValue())
    println(ls.text)
    def mostRecentTag = "git describe --tags".execute([], file)
    mostRecentTag.waitFor()
    println(mostRecentTag.exitValue())
    println(mostRecentTag.text)
    if (mostRecentTag.exitValue() == 0) {
        currentTag = mostRecentTag.text.trim()
    } else {
        currentTag = "0.0.0"
    }
    return currentTag
}