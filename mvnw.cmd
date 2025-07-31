@REM
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM   http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM

@REM -----------------------------------------------------------------------------
@REM Maven Wrapper script
@REM
@REM Environment Variable Prerequisites
@REM
@REM   JAVA_HOME       Must point at your Java Development Kit installation.
@REM   MAVEN_OPTS      (Optional) Java runtime options used when running Maven.
@REM   MAVEN_HOME      (Optional) Maven installation directory.
@REM
@REM -----------------------------------------------------------------------------

@echo off
setlocal

set MAVEN_WRAPPER_VERSION=3.2.0

set MAVEN_WRAPPER_DIR=
if not "%MAVEN_WRAPPER_DIR%"=="" (
  if exist "%MAVEN_WRAPPER_DIR%" (
    for %%i in ("%MAVEN_WRAPPER_DIR%") do set "MAVEN_WRAPPER_DIR=%%~fi"
  )
) else if exist "%~dp0.mvn\wrapper" (
  for %%i in ("%~dp0.mvn\wrapper") do set "MAVEN_WRAPPER_DIR=%%~fi"
) else (
  for %%i in ("%~dp0") do set "MAVEN_WRAPPER_DIR=%%~fi"
)

set MAVEN_WRAPPER_JAR_PATH=
if not "%MAVEN_WRAPPER_JAR%"=="" (
  if exist "%MAVEN_WRAPPER_JAR%" (
    set MAVEN_WRAPPER_JAR_PATH="%MAVEN_WRAPPER_JAR%"
  )
) else if exist "%MAVEN_WRAPPER_DIR%\maven-wrapper.jar" (
  set MAVEN_WRAPPER_JAR_PATH="%MAVEN_WRAPPER_DIR%\maven-wrapper.jar"
)

if "%MAVEN_WRAPPER_JAR_PATH%"=="" (
  if exist "%MAVEN_WRAPPER_DIR%\maven-wrapper-%MAVEN_WRAPPER_VERSION%.jar" (
    set MAVEN_WRAPPER_JAR_PATH="%MAVEN_WRAPPER_DIR%\maven-wrapper-%MAVEN_WRAPPER_VERSION%.jar"
  )
)

if "%MAVEN_WRAPPER_JAR_PATH%"=="" (
  echo Downloading Maven Wrapper %MAVEN_WRAPPER_VERSION%...
  if not exist "%MAVEN_WRAPPER_DIR%" (
    mkdir "%MAVEN_WRAPPER_DIR%"
  )
  set MAVEN_WRAPPER_JAR_PATH="%MAVEN_WRAPPER_DIR%\maven-wrapper-%MAVEN_WRAPPER_VERSION%.jar"
  set MAVEN_WRAPPER_DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/%MAVEN_WRAPPER_VERSION%/maven-wrapper-%MAVEN_WRAPPER_VERSION%.jar

  if not "%MAVEN_WRAPPER_URL%"=="" (
    set MAVEN_WRAPPER_DOWNLOAD_URL=%MAVEN_WRAPPER_URL%
  )

  echo -n "Downloading from: "
  echo %MAVEN_WRAPPER_DOWNLOAD_URL%
  
  bitsadmin /transfer "mvnw_download" %MAVEN_WRAPPER_DOWNLOAD_URL% %MAVEN_WRAPPER_JAR_PATH% > NUL
  if errorlevel 1 (
    powershell -Command "Invoke-WebRequest -Uri %MAVEN_WRAPPER_DOWNLOAD_URL% -OutFile %MAVEN_WRAPPER_JAR_PATH%"
  )
  
  if errorlevel 1 (
    echo "Could not find bitsadmin or PowerShell. Please download Maven Wrapper %MAVEN_WRAPPER_VERSION% manually."
    exit /b 1
  )
)

set MAVEN_WRAPPER_PROPERTIES=
if "%MAVEN_WRAPPER_PROPERTIES%"=="" (
  if exist "%MAVEN_WRAPPER_DIR%\maven-wrapper.properties" (
    set MAVEN_WRAPPER_PROPERTIES="%MAVEN_WRAPPER_DIR%\maven-wrapper.properties"
  )
)

"%JAVA_HOME%\bin\java.exe" %MAVEN_OPTS% -jar %MAVEN_WRAPPER_JAR_PATH% -Dmaven.wrapper.properties=%MAVEN_WRAPPER_PROPERTIES% %*