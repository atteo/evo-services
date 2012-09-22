#!/bin/bash

#
# Script to execute the application directly from Maven repository without creating the WAR file.
# In normal circumstances the WAR file contains all dependend libraries in WEB-INF/lib directory.
# This script configures the classpath to point directly to Maven repository to search
# for any libraries and so the time consuming process of building the WAR file is not needed.
#
# Configuration:
# 1. Set the ROOT variable below to the name of the project to start
ROOT=""
#
# Usage:
#
# 1. Execute the application
#
# ./start.sh
#
# 2. Recompile the given projects and then execute the application
#
# ./start.sh gitserver
#
# 3. Start the application in debug mode
#
# ./start.sh --debug
#

EXTRA=""

while [[ "$1" != "" ]]; do
	if [[ "$1" == "--debug" ]] || [[ "$1" == "-d" ]]; then
		EXTRA="$EXTRA -Xdebug -Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=y"
		shift
		continue
	fi

	pushd "$1"
	mvn clean install
	popd
	shift
done


OLDEST_POM_DATE="$(find . -name pom.xml -exec stat -c %Y {} + | sort | tail -1)"
CLASSPATH_DATE="$(stat -c %Y .direct_classpath 2>/dev/null)"

classpath=

if [[ -e ".direct_classpath" ]] && (( OLDEST_POM_DATE < CLASSPATH_DATE  )); then
	classpath="$(cat .direct_classpath)"
else

	echo "Calculating classpath for in-place execution..."
	next=false


	while read LINE; do
		if [[ "$next" == "true" ]]; then
			classpath="$LINE"
			break
		fi
		if echo "$LINE" | grep "Dependencies classpath:" > /dev/null; then
			next="true"
		fi
	done < <( cd $ROOT; mvn dependency:build-classpath )
	echo "$classpath" > .direct_classpath
fi

cd $ROOT >/dev/null 2>&1 || true
java $EXTRA -cp $classpath:target/$ROOT-*:target/$ROOT-*/WEB-INF/classes Start --expanded target/$ROOT-*/

