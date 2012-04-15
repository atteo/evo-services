#!/bin/bash

#
# Start HSQLDB console
#

if [ ! -e ~/tmp/mop.jar ]; then 
	wget http://mop.fusesource.org/repo/release/org/fusesource/mop/mop-core/1.0-m1/mop-core-1.0-m1.jar -O ~/tmp/mop.jar
fi

java -jar ~/tmp/mop.jar exec org.hsqldb:hsqldb:2.3.0 +postgresql:postgresql:9.0-801.jdbc4 +mysql:mysql-connector-java:5.1.14 +org.apache.derby:derby:10.7.1.1 org.hsqldb.util.DatabaseManagerSwing "$@"

