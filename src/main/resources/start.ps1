#!/usr/bin/env bash
# requires: Java, Maven, PowerShell, Permission to run PS scripts
# permissions for this PS session only:   Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process

# exit if cmdlet gives error
$ErrorActionPreference = "Stop"

# Check to see if root CA file exists, download if not
If (!(Test-Path ".\root-CA.crt")) {
    "`nDownloading AWS IoT Root CA certificate from AWS..."
    Invoke-WebRequest -Uri https://www.amazontrust.com/repository/AmazonRootCA1.pem -OutFile root-CA.crt
}

If (!(Test-Path ".\aws-iot-device-sdk-java-v2")) {
    "`nInstalling AWS SDK..."
    git clone https://github.com/aws/aws-iot-device-sdk-java-v2.git --recursive
    cd aws-iot-device-sdk-java-v2
    mvn versions:use-latest-versions --% -Dincludes="software.amazon.awssdk.crt*"
    mvn clean install -D"maven.test.skip"="true"
    cd ..
}

"`nRunning the pub/sub sample application..."
cd aws-iot-device-sdk-java-v2
mvn exec:java -pl samples/BasicPubSub --% -Dexec.mainClass="pubsub.PubSub" -Dexec.args="--endpoint aia19hth4m2a-ats.iot.eu-central-1.amazonaws.com --client_id sdk-java --topic sdk/test/java --ca_file ../root-CA.crt --cert ../test_fek_03.cert.pem --key ../test_fek_03.private.key"