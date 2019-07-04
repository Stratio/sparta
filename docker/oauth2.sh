#!/bin/bash

INFO "[OAUTH] Getting oauth secrets from vault"
#Downloads client id and secrets for the app

getPass "userland" "$SERVICE_ID_WITH_PATH" "oauthinfo"

INFO "[OAUTH] Exporting sparta oauth variables"

export OAUTH2_ENABLE=${SECURITY_OAUTH2_ENABLE}
OAUTH_ID_VARIABLE=${SERVICE_ID_WITH_PATH_NORM}_OAUTHINFO_USER
export OAUTH2_CLIENT_ID=${!OAUTH_ID_VARIABLE}
OAUTH_PASS_VARIABLE=${SERVICE_ID_WITH_PATH_NORM}_OAUTHINFO_PASS
export OAUTH2_CLIENT_SECRET=${!OAUTH_PASS_VARIABLE}
