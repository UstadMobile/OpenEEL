#!/bin/bash

# Script used on CI (Continuous Integration - eg Jenkins) to run a test server process for a specific
# test. This script will be run by TestServerController
# (see https://github.com/UstadMobile/TestServerController ). You SHOULD NOT run it manually unless
# required to debug the script itself.

# CI (Jenkins Scenario)
# a) ci-run-e2e-tests.sh starts TestServerController (where run command is set to this script).
#    ci-run-e2e-tests.sh will save the admin password to dir-admin.txt in advance (allowing the test
#    itself to add a new school)
# b) Maestro test calls TESTSERVERCONTROLLER_URL/start to start server process and get the allocated
#     port. ci-run-test-server.sh also sets a shutdown url
# c) Maestro test uses the server process that was started. This can be using the created URL directly
#    or by string substitution (e.g. https://portnum.example.org/ where a reverse proxy is setup).
# d) Maestro test calls /stop?port=(portnum) to stop the server.

# TESTSERVER_WORKSPACE and TESSTSERVER_PORT is set by TestServerController.
# VERSION is set in ci-run-e2e-tests.sh
echo "ci-run-test-server.sh: Workspace = $TESTSERVER_WORKSPACE Port=$TESTSERVER_PORT"

# start keycloak server and adding realm
if [ -n "${KEYCLOAK_ADMIN_PASSWORD:-}" ]; then
    kc_name=${KEYCLOAK_CONTAINER_NAME:-openeel-keycloak}
    kc_port=${KEYCLOAK_PORT:-8080}
    kc_host=${KEYCLOAK_HOST:-$(hostname -I | awk '{print $1}')}
    kc_url=${KEYCLOAK_URL:-http://${kc_host:-127.0.0.1}:$kc_port}
    export KEYCLOAK_URL="$kc_url"
    kc_admin=${KEYCLOAK_ADMIN_USERNAME:-admin}

    if docker inspect "$kc_name" >/dev/null 2>&1; then
        [ "$(docker inspect -f '{{.State.Running}}' "$kc_name")" = "true" ] || docker start "$kc_name" >/dev/null || exit 1
    else
        export KC_BOOTSTRAP_ADMIN_PASSWORD="$KEYCLOAK_ADMIN_PASSWORD"
        docker run -d --name "$kc_name" -p "0.0.0.0:$kc_port:8080" \
            -e "KC_BOOTSTRAP_ADMIN_USERNAME=$kc_admin" -e KC_BOOTSTRAP_ADMIN_PASSWORD \
            -v openeel-keycloak-data:/opt/keycloak/data \
            quay.io/keycloak/keycloak:26.8.0 start-dev --hostname="$kc_url" >/dev/null || exit 1
        unset KC_BOOTSTRAP_ADMIN_PASSWORD
    fi

fi

ROOTDIR=$(realpath $(dirname $BASH_SOURCE))

echo $ROOTDIR

unzip -q -d $TESTSERVER_WORKSPACE $ROOTDIR/app-server/build/distributions/app-server-$VERSION.zip

DATADIR=$TESTSERVER_WORKSPACE/data
mkdir -p $DATADIR

# Set the directory server admin authentication (passed from the TestServerController)
echo $DIR_ADMIN_AUTH > $DATADIR/dir-admin.txt

echo "ci-run-test-server.sh: saved admin auth to $DATADIR/dir-admin.txt"

export JAVA_OPTS="-Dlogs_dir=$TESTSERVER_WORKSPACE/logs/"
echo "ci-run-test-server.sh starting server :"
echo $TESTSERVER_WORKSPACE/app-server-$VERSION/bin/app-server runserver \
          -P:ktor.deployment.port=$TESTSERVER_PORT \
          -P:ktor.respect.datadir=$TESTSERVER_WORKSPACE/data \
          -P:ktor.e2eartifactupload.enabled=true \
          -P:ktor.pidfile=$SERVER_PIDFILE \

# Could set the credentials required to create a new instance here.
$TESTSERVER_WORKSPACE/app-server-$VERSION/bin/app-server runserver \
     -P:ktor.deployment.port=$TESTSERVER_PORT \
     -P:ktor.respect.datadir=$TESTSERVER_WORKSPACE/data \
     -P:ktor.e2eartifactupload.enabled=true \
     -P:ktor.pidfile=$SERVER_PIDFILE \

