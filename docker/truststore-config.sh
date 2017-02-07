#!/bin/bash


#Downloads all certs from vault
#This wil be saved in /etc/sds/sparta/security/truststore.jks
#The password for the keystore is saved to $DEFAULT_KEYSTORE_PASS
_log_sparta_sec "Getting accepted certs from vault"
getCAbundle "/etc/sds/sparta/security" JKS "truststore.jks"


#Add all certs from the truststore to the java truststore
#TODO Vault: availability for default truststore for java
export XD_JVMCA_PASS="changeit"
_log_sparta_sec "Adding certs to java trust strore"
keytool -importkeystore -srckeystore "/etc/sds/sparta/security/truststore.jks" -destkeystore $JAVA_HOME/jre/lib/security/cacerts \
    -srcstorepass "$DEFAULT_KEYSTORE_PASS" -deststorepass "$XD_JVMCA_PASS"
