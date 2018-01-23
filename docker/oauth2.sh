#!/bin/bash

INFO "[OAUTH] Getting oauth secrets from vault"
#Downloads client id and secrets for the app

getPass "userland" "$TENANT_NAME" "oauthinfo"

INFO "[OAUTH] Exporting sparta oauth variables"

export OAUTH2_ENABLE=${SECURITY_OAUTH2_ENABLE}
OAUTH_ID_VARIABLE=${TENANT_NORM}_OAUTHINFO_USER
export OAUTH2_CLIENT_ID=${!OAUTH_ID_VARIABLE}
OAUTH_PASS_VARIABLE=${TENANT_NORM}_OAUTHINFO_PASS
export OAUTH2_CLIENT_SECRET=${!OAUTH_PASS_VARIABLE}
