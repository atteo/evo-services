#!/bin/bash

# By default unit tests which require some additional configuration are disabled.
# You may execute them by enabling "difficult-tests" Maven profile like this:

# mvn clean test -PmoreTests

# But before you do so, you need to configure your environment accordingly:


####################
# PostgreSQL

# Install PostgreSQL. Create user with database for the tests.
# Set the following environment variables:
export MOONSHINE_POSTGRESQL_SERVERNAME=
export MOONSHINE_POSTGRESQL_PORTNUMBER=
export MOONSHINE_POSTGRESQL_DATABASENAME=
export MOONSHINE_POSTGRESQL_USER=
export MOONSHINE_POSTGRESQL_PASSWORD=

####################
# WebDriver

# Install chromedriver from http://chromedriver.storage.googleapis.com/index.html


mvn clean test -PmoreTests "$@"
