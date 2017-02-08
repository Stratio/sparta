#!/usr/bin/env bash

echo "Delete all but the original scripts:"
find ./ -type f ! \(  -name 'createDocker.sh' -o  -name 'Dockerfile'  -o -name 'dependencyfix.sh'  -o -name 'README.md' \) -exec  rm -v  {} +

echo "Copy parent scripts"
cp -v ../*.sh .

chmod +x *.sh

echo "Copy dist deb to PKG.deb"
cp -v ../../dist/target/*.deb .
mv -v  *.deb PKG.deb

echo "Generate docker"
docker build . -t sparta
