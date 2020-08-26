#!/bin/sh

mainjar="logviewer.jar"
export BASE_DIR=`cd $(dirname $0); pwd`
pid=`ps ax | grep -i ${mainjar} | grep ${BASE_DIR} | grep java | grep -v grep | awk '{print $1}'`
if [ -n "$pid" ] ; then
        echo "logviewer is running."
        exit -1;
fi

cygwin=false
darwin=false
os400=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Darwin*) darwin=true;;
OS400*) os400=true;;
esac
error_exit ()
{
    echo "ERROR: $1 !!"
    exit 1
}
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=$HOME/jdk/java
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=/usr/java
[ ! -e "$JAVA_HOME/bin/java" ] && unset JAVA_HOME

if [ -z "$JAVA_HOME" ]; then
  if $darwin; then

    if [ -x '/usr/libexec/java_home' ] ; then
      export JAVA_HOME=`/usr/libexec/java_home`

    elif [ -d "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home" ]; then
      export JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home"
    fi
  else
    JAVA_PATH=`dirname $(readlink -f $(which javac))`
    if [ "x$JAVA_PATH" != "x" ]; then
      export JAVA_HOME=`dirname $JAVA_PATH 2>/dev/null`
    fi
  fi
  if [ -z "$JAVA_HOME" ]; then
        error_exit "Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better!"
  fi
fi

export JAVA_HOME
export JAVA="$JAVA_HOME/bin/java"


#===========================================================================================
# JVM Configuration
#===========================================================================================
JAVA_OPT="-server -Xms128m -Xmx128m -Xss256k"
JAVA_OPT="${JAVA_OPT} -jar ${BASE_DIR}/${mainjar}"

echo "$JAVA ${JAVA_OPT}"

nohup $JAVA ${JAVA_OPT} > ${BASE_DIR}/console.out 2>&1 &
echo "logviewer starting..."
