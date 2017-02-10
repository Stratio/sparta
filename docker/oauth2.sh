#!/bin/bash

_log_sparta_sec "Getting oauth secrets from vault"
#Downloads client id and secrets for the app
getPass "userland" "sparta" "oauthinfo"

_log_sparta_sec "Exporting sparta ouath variables"
#TODO: When gosec is available config the app
#export OAUTH2_ENABLE=true
export OAUTH2_CLIENT_ID=$SPARTA_OAUTHINFO_USER
export OAUTH2_CLIENT_SECRET=$SPARTA_OAUTHINFO_PASS
