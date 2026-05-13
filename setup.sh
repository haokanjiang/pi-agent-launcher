#!/bin/bash
# Bootstrap script to set up Gradle wrapper
# Run this once: ./setup.sh

GRADLE_VERSION="8.10"
WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading Gradle wrapper..."
    mkdir -p gradle/wrapper
    curl -sL "https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VERSION}/gradle/wrapper/gradle-wrapper.jar" -o "$WRAPPER_JAR"
    
    if [ ! -f "$WRAPPER_JAR" ]; then
        echo "Failed to download wrapper jar. Please install Gradle and run: gradle wrapper --gradle-version $GRADLE_VERSION"
        exit 1
    fi
fi

echo "Gradle wrapper ready. Run: ./gradlew build"
