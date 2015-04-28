#!/bin/sh
find $1 -type f -name "*.jar" | xargs -i   cp {} $2
