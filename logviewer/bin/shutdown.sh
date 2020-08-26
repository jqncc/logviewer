#!/bin/sh

cd `dirname $0`
target_dir=`pwd`

pid=`ps ax | grep -i 'logviewer.jar' | grep ${target_dir} | grep java | grep -v grep | awk '{print $1}'`
if [ -z "$pid" ] ; then
        echo "No logviewer running."
        exit -1;
fi
kill ${pid}

echo "shutdown logviewer(${pid})"
