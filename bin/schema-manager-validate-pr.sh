#!/bin/sh

BASEDIR=$(dirname "$0")

java -cp "$BASEDIR/../schemamanager-0.0.1-SNAPSHOT.jar:$BASEDIR/../lib/*" \
    com.amazonaws.schemamanager.SchemaManagerMain -m pr -c $BASEDIR/../conf/pr_validation_config.yml