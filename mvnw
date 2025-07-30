#!/bin/sh
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

# -----------------------------------------------------------------------------
# Maven Wrapper script
#
# Environment Variable Prerequisites
#
#   JAVA_HOME       Must point at your Java Development Kit installation.
#   MAVEN_OPTS      (Optional) Java runtime options used when running Maven.
#   MAVEN_HOME      (Optional) Maven installation directory.
#
# -----------------------------------------------------------------------------

MAVEN_WRAPPER_VERSION="3.2.0"

#
# Resolve the MAVEN_WRAPPER_DIR, checking in order:
# - MAVEN_WRAPPER_DIR environment variable
# - .mvn/wrapper directory
#
if [ -n "$MAVEN_WRAPPER_DIR" ]; then
    MAVEN_WRAPPER_DIR="$(cd "$MAVEN_WRAPPER_DIR" && pwd)"
elif [ -d "$(dirname "$0")/.mvn/wrapper" ]; then
    MAVEN_WRAPPER_DIR="$(cd "$(dirname "$0")/.mvn/wrapper" && pwd)"
else
    # Default to the location of the script
    MAVEN_WRAPPER_DIR="$(cd "$(dirname "$0")" && pwd)"
fi

#
# Check if the MAVEN_WRAPPER_JAR is specified and exists.
# If not, check for the default jar in the MAVEN_WRAPPER_DIR.
#
if [ -n "$MAVEN_WRAPPER_JAR" ] && [ -f "$MAVEN_WRAPPER_JAR" ]; then
    MAVEN_WRAPPER_JAR_PATH="$MAVEN_WRAPPER_JAR"
elif [ -f "$MAVEN_WRAPPER_DIR/maven-wrapper.jar" ]; then
    MAVEN_WRAPPER_JAR_PATH="$MAVEN_WRAPPER_DIR/maven-wrapper.jar"
fi

if [ -z "$MAVEN_WRAPPER_JAR_PATH" ] && [ -f "$MAVEN_WRAPPER_DIR/maven-wrapper-${MAVEN_WRAPPER_VERSION}.jar" ]; then
    MAVEN_WRAPPER_JAR_PATH="$MAVEN_WRAPPER_DIR/maven-wrapper-${MAVEN_WRAPPER_VERSION}.jar"
fi

if [ -z "$MAVEN_WRAPPER_JAR_PATH" ]; then
    #
    # The MAVEN_WRAPPER_JAR is not specified and the default jar does not exist.
    # So, we are downloading the wrapper jar.
    #
    echo "Downloading Maven Wrapper ${MAVEN_WRAPPER_VERSION}..."
    if [ ! -d "$MAVEN_WRAPPER_DIR" ]; then
      mkdir -p "$MAVEN_WRAPPER_DIR"
    fi
    MAVEN_WRAPPER_JAR_PATH="$MAVEN_WRAPPER_DIR/maven-wrapper-${MAVEN_WRAPPER_VERSION}.jar"
    MAVEN_WRAPPER_DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/${MAVEN_WRAPPER_VERSION}/maven-wrapper-${MAVEN_WRAPPER_VERSION}.jar"

    if [ -n "$MAVEN_WRAPPER_URL" ]; then
      MAVEN_WRAPPER_DOWNLOAD_URL="$MAVEN_WRAPPER_URL"
    fi

    if [ -n "$(command -v wget)" ]; then
        wget -q -O "$MAVEN_WRAPPER_JAR_PATH" "$MAVEN_WRAPPER_DOWNLOAD_URL"
    elif [ -n "$(command -v curl)" ]; then
        curl -L -s -o "$MAVEN_WRAPPER_JAR_PATH" "$MAVEN_WRAPPER_DOWNLOAD_URL"
    else
        echo "Could not find wget or curl. Please download Maven Wrapper ${MAVEN_WRAPPER_VERSION} manually."
        exit 1
    fi
fi

#
# A MAVEN_WRAPPER_JAR is specified and exists.
#
if [ -z "$MAVEN_WRAPPER_PROPERTIES" ] && [ -f "$MAVEN_WRAPPER_DIR/maven-wrapper.properties" ]; then
    MAVEN_WRAPPER_PROPERTIES="$MAVEN_WRAPPER_DIR/maven-wrapper.properties"
fi

exec "$JAVA_HOME/bin/java" ${MAVEN_OPTS} -jar "$MAVEN_WRAPPER_JAR_PATH" -Dmaven.wrapper.properties="$MAVEN_WRAPPER_PROPERTIES" "$@"