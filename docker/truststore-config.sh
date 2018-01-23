#!/bin/bash


#Downloads all certs from vault
#This wil be saved in /etc/sds/sparta/security/truststore.jks
#The password for the keystore is saved to $DEFAULT_KEYSTORE_PASS
INFO "[TRUSTSTORE-CONFIG] Getting accepted certs from vault"
getCAbundle "/etc/sds/sparta/security" JKS "truststore.jks"


#Add all certs from the truststore to the java truststore
#TODO Vault: availability for default truststore for java
export JVMCA_PASS="changeit"
INFO "[TRUSTSTORE-CONFIG] Adding certs to java trust store"
keytool -importkeystore -srckeystore "/etc/sds/sparta/security/truststore.jks" -destkeystore $JAVA_HOME/jre/lib/security/cacerts \
    -srcstorepass "$DEFAULT_KEYSTORE_PASS" -deststorepass "$JVMCA_PASS" -noprompt

export SPARTA_TRUSTSTORE_PASSWORD=$DEFAULT_KEYSTORE_PASS