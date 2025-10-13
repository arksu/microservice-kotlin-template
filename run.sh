#!/bin/bash

# Define base directory
BASE_DIR="$(cd "$(dirname "$0")" && pwd)/build/libs"

# Define main JAR and lib directory
APP_JAR="$BASE_DIR/app.jar"
LIB_DIR="$BASE_DIR/lib"

# Build classpath: include all jars in lib plus app.jar
CLASSPATH="$APP_JAR"
for jar in "$LIB_DIR"/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done

# Run the application
java -cp "$CLASSPATH" io.ktor.server.netty.EngineMain
