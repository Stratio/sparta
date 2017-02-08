#!/bin/bash

 deps=`dpkg --info $1 | grep -i depends | sed -n "s/.*depends: //ip"`
 export DEBIAN_FRONTEND=noninteractive

 IFS=',' read -ra dep <<< "$deps"
 for i in "${dep[@]}"; do
     echo $i
     elver=`echo "$i" | sed -e "s/[() ]//gp"`
     echo $elver
     apt-get -y install $elver
 done
