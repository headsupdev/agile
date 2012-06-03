#! /bin/sh
#leave the space above, maven resources plugin bug - MRESOURCES-110

THIS="$0"
DIR=`dirname "$THIS"`
HEADSUP_HOME=`cd "$DIR/.."; pwd`
cd $HEADSUP_HOME

if [ !  -d logs ]; then
  mkdir logs
fi

JAVA_EXEC=$JAVA_HOME/bin/java
if [ !  -x $JAVA_EXEC ]; then

  JAVA_EXEC=`which java`
  if [ -z $JAVA_EXEC ] || [ !  -x $JAVA_EXEC ]; then
    echo "[ERROR] unable to find java - please set JAVA_HOME or add java to your PATH"
    exit
  fi

fi

JAVA_OPTS="-Xmx512m -Djava.awt.headless=true -Duser.timezone=UTC"

COLORS=''
COLOURCOUNT=$(tput colors 2> /dev/null)
if [ $? = 0 ] && [ $COLOURCOUNT -gt 2 ]; then
    COLORS='-Dagile.runtime.color=true'
fi

TIMEZONE=''
# Detect timezone for various unix flavours
if [ -x /etc/timezone ]; then
    TIMEZONE='-Dagile.runtime.timezone='`cat /etc/timezone`

#then try Mac OSX
elif [ `which systemsetup` ]; then
    SYSTEMZONE=`systemsetup -gettimezone | sed 's/^.*: //g'`
    if [[ "$SYSTEMZONE" !\= *"..."* ]]; then
        TIMEZONE='-Dagile.runtime.timezone='$SYSTEMZONE
    fi
fi

$JAVA_EXEC $JAVA_OPTS $COLORS $TIMEZONE \
 -cp conf:bin/${project.artifactId}-${project.version}.jar \
 org.headsupdev.agile.runtime.Main $@ 2>&1 | tee -i logs/agile.log
