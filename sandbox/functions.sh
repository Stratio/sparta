#!/bin/bash

checkNotExistsFile ()
{

    checkParameters $1 1

    # Directory
    file=$1

    # Check that is a file and exist
    if [ -e "${file}" ]; then
        # Show error and exit
        echo 1<&2 "ERROR: $0: File: ${file} exists, please make sure that file does not exists"
        exit 2
    fi
}

deleteIfExists ()
{
    checkParameters $1 1
    file=$1
    if [ -e "${file}" ]; then
        echo 1<&2 "INFO: $0: File: ${file} exists, deleting it..."
        rm ${file}
    fi
    if [ -d "${file}" ]; then
        echo 1<&2 "INFO: $0: Folder: ${file} exists, deleting it..."
        rm -r ${file}
    fi
}

checkParameters ()
{
    last=${@: -1}
    expected=$(($last+1))
    # Check that we have one parameter
    if [ $# -ne ${expected} ]; then
        echo 1>&2 "ERROR: $0: You're expecting ${last} arguments, but we're receiving $(($#-1))"
        # Exit entire script
        exit 1
    fi
}
