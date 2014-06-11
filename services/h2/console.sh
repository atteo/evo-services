#!/bin/bash

#
# Start H2 database and console
#

if [ ! -e ~/tmp/mop.jar ]; then
	wget http://mop.fusesource.org/repo/release/org/fusesource/mop/mop-core/1.0-m1/mop-core-1.0-m1.jar -O ~/tmp/mop.jar
fi


echo "Database URL for tests:"
echo ""
echo " jdbc:h2:target/test-home/database;AUTO_SERVER=TRUE"
echo ""

cd "$( dirname "${BASH_SOURCE[0]}" )"

mkdir -p target/database

java -jar ~/tmp/mop.jar exec com.h2database:h2:1.3.161 org.h2.tools.Console -webPort 8089

