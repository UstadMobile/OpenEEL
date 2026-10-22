#!/bin/bash

# Script used in CI environment (Continuous Integration - eg Jenkins) to run Maestro end to end tests
# (see .maestro for test flows

ROOTDIR=$(realpath $(dirname $BASH_SOURCE))

CI_E2ERUN_DIR="$ROOTDIR/build/ci-e2e"
if [ ! -e $CI_E2ERUN_DIR ]; then
    mkdir -p $CI_E2ERUN_DIR
fi

# Root directory for TestServerController to use (each server will get its own sub directory)
# TestServerController will create the directory automatically.
TESTSERVERCONTROLLER_BASEDIR="$CI_E2ERUN_DIR/workspace"
if [ ! -e $TESTSERVERCONTROLLER_BASEDIR ]; then
    mkdir -p $TESTSERVERCONTROLLER_BASEDIR
fi

TESTSERVERCONTROLLER_BASENAME="testservercontroller-0.0.15"
TESTSERVERCONTROLLER_DOWNLOAD_URL="https://devserver3.ustadmobile.com/jenkins/job/TestServerController/13/artifact/build/distributions/testservercontroller-0.0.15.zip"

echo "ROOTDIR=$ROOTDIR BASH_SOURCE=$BASH_SOURCE"

if [ ! -e $CI_E2ERUN_DIR/$TESTSERVERCONTROLLER_BASENAME ]; then

    wget --quiet --output-document=$CI_E2ERUN_DIR/$TESTSERVERCONTROLLER_BASENAME.zip \
         $TESTSERVERCONTROLLER_DOWNLOAD_URL

    cp /home/mike/IdeaProjects/TestServerController/build/distributions/$TESTSERVERCONTROLLER_BASENAME.zip \
        $CI_E2ERUN_DIR/$TESTSERVERCONTROLLER_BASENAME.zip

    unzip -q -d $CI_E2ERUN_DIR $CI_E2ERUN_DIR/$TESTSERVERCONTROLLER_BASENAME.zip
fi

TESTCONTROLLER_BIN=$CI_E2ERUN_DIR/$TESTSERVERCONTROLLER_BASENAME/bin/testservercontroller

if [ "$TESTCONTROLLER_PORT" == "" ]; then
    TESTCONTROLLER_PORT=8094
fi

if [ "$TESTCONTROLLER_URL" == "" ]; then
    if [ "$URL_SUBSTITUTION" != "" ]; then
        TESTCONTROLLER_URL=$(echo $URL_SUBSTITUTION | sed s/_PORT_/$TESTCONTROLLER_PORT/g)
    else
        echo "ci-run-maestro: no TESTCONTROLLER_URL set: using hostname - this might not be correct"
        TESTCONTROLLER_URL="http://$(hostname -I | awk '{print $1}'):$TESTCONTROLLER_PORT/"
    fi
fi

echo "ci-run-maestro: TESTCONTROLLER_URL is $TESTCONTROLLER_URL"

if [ "$TEST_LEARNINGSPACE_PORTRANGE" == "" ]; then
    TEST_LEARNINGSPACE_PORTRANGE="8000-9000"
fi

function create_test_artifact_zip() {
    echo "ci-run-maestro: Archiving test artifacts..."

    local OUTPUT_ZIP="$CI_E2ERUN_DIR/artifacts/EndToEnd_Test_Artifacts_$(date +%Y%m%d_%H%M%S).zip"
    if [ ! -e "$(dirname $OUTPUT_ZIP)" ]; then
        mkdir -p $(dirname $OUTPUT_ZIP)
    fi

    if [ -d "$TESTSERVERCONTROLLER_BASEDIR" ]; then
        cd "$TESTSERVERCONTROLLER_BASEDIR"

        zip -q -r "$OUTPUT_ZIP" . \
            -i "logs/*" \
            -i "*/data/*" \
            -i "*/logs/*" \
            -i "*/logcat.txt" \
            -i "maestro/*"

        cd "$ROOTDIR"
        echo "ci-run-maestro: Artifacts zipped to $OUTPUT_ZIP"
    else
        echo "ci-run-maestro: Workspace directory not found, skipping zip."
    fi
}

function check_databases() {
    while read -r db; do
        if [ "$(sqlite3 "$db" "PRAGMA integrity_check;")" != "ok" ]; then
            echo "ci-run-e2e-tests: FAIL - database does not pass integrity check: $db"
            exit 1
        fi
    done < <(find "$TESTSERVERCONTROLLER_BASEDIR" -path "*/e2e-client-artifacts/*.db")
}

function cleanup() {
    if [ "$TESTCONTROLLER_PID" != "" ]; then
        echo "ci-run-e2e-tests: Stopping TestServerController : pid=$TESTCONTROLLER_PID"

        kill -SIGINT $TESTCONTROLLER_PID
        pkill -SIGINT -P $TESTCONTROLLER_PID
        sleep 10
        kill $TESTCONTROLLER_PID
        pkill -P $TESTCONTROLLER_PID
    fi

    create_test_artifact_zip
}

trap cleanup EXIT


DIR_ADMIN_AUTH_PASS=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 13)
if [ "$SCHOOL_ADMIN_PASSWORD" == "" ]; then
    SCHOOL_ADMIN_PASSWORD=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 13)
fi

if [ "$GIT_TAG_NAME" != "" ]; then
    VERSION=$GIT_TAG_NAME
else
    GRADLE_PROP_LINE=$(grep version= $ROOTDIR/gradle.properties)

    # Use bash parameter expansion to remove version=prefix
    VERSION=${GRADLE_PROP_LINE#version=}
fi

# The Maestro test needs to use basic auth (which is base64 encoded) to authenticate to request the
# creation of the school, that is encoded here and passed to Maestro to avoid using Maestro's
# Javascript (which does not have the btoa function)
DIR_ADMIN_TO_ENCODE="admin:$DIR_ADMIN_AUTH_PASS"
DIR_ADMIN_AUTH_HEADER="Basic $(printf '%s' $DIR_ADMIN_TO_ENCODE | base64)"

# Explicitly set to empty string if unset
if [ "$MAESTRO_EXTRA_ARGS" == "" ]; then
   MAESTRO_EXTRA_ARGS=""
fi

if [ "$MAESTRO_FLOW" == "" ]; then
    MAESTRO_FLOW=.maestro/flows/*.yaml
fi

export JAVA_OPTS="-Dlogs_dir=$TESTSERVERCONTROLLER_BASEDIR/logs/"
$TESTCONTROLLER_BIN  \
    -P:ktor.deployment.port=$TESTCONTROLLER_PORT \
    -P:testservercontroller.portRange=$TEST_LEARNINGSPACE_PORTRANGE \
    -P:testservercontroller.urlsubstitution=$URL_SUBSTITUTION \
    -P:testservercontroller.basedir=$TESTSERVERCONTROLLER_BASEDIR \
    -P:testservercontroller.env.DIR_ADMIN_AUTH=$DIR_ADMIN_AUTH_PASS \
    -P:testservercontroller.env.VERSION=$VERSION \
    -P:testservercontroller.cmd="$ROOTDIR/ci-run-test-server.sh" &

TESTCONTROLLER_PID=$!

wait-port $TESTCONTROLLER_PORT
echo "ci-run-e2e-tests: TestServerController now running on port $TESTCONTROLLER_PORT (pid $TESTCONTROLLER_PID)"

# Can now run maestro - the TESTSERVERCONTROLLER url is known and we also know the admin auth to create a new school etc.

echo "Run Maestro using $TESTSERVERCONTROLLER_URL"

MAESTRO_OUTPUT_DIR=$TESTSERVERCONTROLLER_BASEDIR/maestro/output
MAESTRO_REPORT_FILE=$TESTSERVERCONTROLLER_BASEDIR/maestro/report.xml

if [ ! -e $MAESTRO_OUTPUT_DIR ]; then
    mkdir -p $MAESTRO_OUTPUT_DIR
fi

TEST_APP_URL_ARG=""

if [ "$TEST_APP_URL" != "" ]; then
    TEST_APP_URL_ARG=" --env TEST_APP_URL=$TEST_APP_URL "
fi

if [ "$1" == "cloud" ]; then
    if [ "$MAESTRO_CLOUD_PROJECTID" == "" ]; then
      echo "Must set Maestro cloud project id as MAESTRO_CLOUD_PROJECTID environment var"
      exit 1
    fi

    if [ "$MAESTRO_CLOUD_APIKEY" == "" ]; then
      echo "Must set Maestro cloud API key as MAESTRO_CLOUD_APIKEY environment var"
      exit 1
    fi

    BRANCH_ARG=""
    PULLREQUEST_ARG=""
    NAME_ARG=""
    COMMIT_ARG=""
    BRANCH_ARG=""
    DEVICE_OS_ARG=""

    if [ "$BUILD_TAG" != "" ]; then
        NAME_ARG="--name=$BUILD_TAG"
    fi

    if [ "$GIT_BRANCH" != "" ]; then
        BRANCH_ARG="--branch=$BRANCH"
    fi

    if [ "$GIT_COMMIT" != "" ]; then
        COMMIT_ARG="--commit-sha=$GIT_COMMIT"
    fi

    if [ "$PULLREQUEST" != "" ]; then
        PULLREQUEST_ARG="--pull-request-id=$PULLREQUEST"
    fi

     if [ "$DEVICE_OS_ARG" == "" ]; then
           DEVICE_OS_ARG="--device-os=android-35"
     fi

    MAESTRO_LOG_FILE="$TESTSERVERCONTROLLER_BASEDIR/lastMaestroRun.log"

    maestro cloud \
        --api-key=$MAESTRO_CLOUD_APIKEY \
        --project-id=$MAESTRO_CLOUD_PROJECTID \
        --app-file=./app-android/build/outputs/apk/release/app-android-release.apk \
        --flows=.maestro/flows \
        $DEVICE_OS_ARG \
        --format=junit \
        --output=$MAESTRO_REPORT_FILE \
        --timeout=300 \
        $NAME_ARG \
        --repo-name=Respect \
        --repo-owner=UstadMobile \
        $COMMIT_ARG \
        $BRANCH_ARG \
        $PULLREQUEST_ARG \
        --env DIR_ADMIN_AUTH_PASS=$DIR_ADMIN_AUTH_PASS \
        --env TESTCONTROLLER_URL=$TESTCONTROLLER_URL \
        --env SCHOOL_ADMIN_PASSWORD=$SCHOOL_ADMIN_PASSWORD \
        --env DIR_ADMIN_AUTH_HEADER="$DIR_ADMIN_AUTH_HEADER" \
        --env SCHOOL_NAME=TestSchool \
        $TEST_APP_URL_ARG \
       | tee $MAESTRO_LOG_FILE  # | tee: Saves to file, Shows on Jenkins Console

    MAESTRO_STATUS=${PIPESTATUS[0]}
    echo "ci-run-e2e-tests: Cloud run finished (Status: $MAESTRO_STATUS). Extracting URL.."


    if [ -f "$MAESTRO_LOG_FILE" ]; then
        # This searches for any URL with stable part of the path — /upload/ — regardless of host
        export MAESTRO_CLOUD_URL=$(grep -oE 'https://[^ ]*/upload/[^ ]*' "$MAESTRO_LOG_FILE" | tail -1)

         if [ -n "$MAESTRO_CLOUD_URL" ]; then
            echo "ci-run-e2e-tests: Found URL: $MAESTRO_CLOUD_URL"

            export MAESTRO_EMAIL="$MAESTRO_EMAIL"
            export RECIVO_API_KEY="$RECIVO_API_KEY"
            export RECIVO_ORG_ID="$RECIVO_ORG_ID"

            echo "ci-run-e2e-tests: Navigating to downloader script..."
            cd "$WORKSPACE/.maestro/video-downloader"

            # Ensure executable
            chmod +x ci-run-cypress.sh

            # Execute the script
            ./ci-run-cypress.sh || echo "ci-run-e2e-tests: Video downloader script encountered an error (ignoring)"

            # Return to original directory
            cd "$WORKSPACE"
         else
            echo "ci-run-e2e-tests: Skipping video download (No Cloud URL found in logs)."
         fi
    else
         echo "ci-run-e2e-tests: Log file not found. Skipping download."
    fi

else

    maestro test \
      --env DIR_ADMIN_AUTH_PASS=$DIR_ADMIN_AUTH_PASS \
      --env TESTCONTROLLER_URL=$TESTCONTROLLER_URL \
      --env SCHOOL_ADMIN_PASSWORD=$SCHOOL_ADMIN_PASSWORD \
      --env DIR_ADMIN_AUTH_HEADER="$DIR_ADMIN_AUTH_HEADER" \
      --env SCHOOL_NAME=TestSchool \
      $TEST_APP_URL_ARG \
      $MAESTRO_EXTRA_ARGS \
      --format=junit \
      --test-output-dir=$MAESTRO_OUTPUT_DIR \
      --output=$MAESTRO_REPORT_FILE \
      $MAESTRO_FLOW
    MAESTRO_STATUS=$?
fi

echo "ci-run-e2e-tests: Maestro test completed. Workspaces are in $TESTSERVERCONTROLLER_BASEDIR"

check_databases

echo "All databases passed integrity check."



exit $MAESTRO_STATUS