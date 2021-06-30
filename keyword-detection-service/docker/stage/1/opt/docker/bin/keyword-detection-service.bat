@REM keyword-detection-service launcher script
@REM
@REM Environment:
@REM JAVA_HOME - location of a JDK home dir (optional if java on path)
@REM CFG_OPTS  - JVM options (optional)
@REM Configuration:
@REM KEYWORD_DETECTION_SERVICE_config.txt found in the KEYWORD_DETECTION_SERVICE_HOME.
@setlocal enabledelayedexpansion
@setlocal enableextensions

@echo off


if "%KEYWORD_DETECTION_SERVICE_HOME%"=="" (
  set "APP_HOME=%~dp0\\.."

  rem Also set the old env name for backwards compatibility
  set "KEYWORD_DETECTION_SERVICE_HOME=%~dp0\\.."
) else (
  set "APP_HOME=%KEYWORD_DETECTION_SERVICE_HOME%"
)

set "APP_LIB_DIR=%APP_HOME%\lib\"

rem Detect if we were double clicked, although theoretically A user could
rem manually run cmd /c
for %%x in (!cmdcmdline!) do if %%~x==/c set DOUBLECLICKED=1

rem FIRST we load the config file of extra options.
set "CFG_FILE=%APP_HOME%\KEYWORD_DETECTION_SERVICE_config.txt"
set CFG_OPTS=
call :parse_config "%CFG_FILE%" CFG_OPTS

rem We use the value of the JAVA_OPTS environment variable if defined, rather than the config.
set _JAVA_OPTS=%JAVA_OPTS%
if "!_JAVA_OPTS!"=="" set _JAVA_OPTS=!CFG_OPTS!

rem We keep in _JAVA_PARAMS all -J-prefixed and -D-prefixed arguments
rem "-J" is stripped, "-D" is left as is, and everything is appended to JAVA_OPTS
set _JAVA_PARAMS=
set _APP_ARGS=

set "APP_CLASSPATH=%APP_LIB_DIR%\ua.edu.ucu.keyword-detection-service-0.1.0.jar;%APP_LIB_DIR%\org.scala-lang.scala-library-2.13.5.jar;%APP_LIB_DIR%\org.apache.kafka.kafka-streams-scala_2.13-2.8.0.jar;%APP_LIB_DIR%\com.sksamuel.avro4s.avro4s-core_2.13-4.0.7.jar;%APP_LIB_DIR%\com.sksamuel.avro4s.avro4s-kafka_2.13-4.0.7.jar;%APP_LIB_DIR%\org.apache.avro.avro-1.10.2.jar;%APP_LIB_DIR%\io.confluent.kafka-streams-avro-serde-6.2.0.jar;%APP_LIB_DIR%\org.apache.kafka.kafka-streams-2.8.0.jar;%APP_LIB_DIR%\org.scala-lang.modules.scala-collection-compat_2.13-2.3.0.jar;%APP_LIB_DIR%\org.scala-lang.scala-compiler-2.13.5.jar;%APP_LIB_DIR%\org.scala-lang.scala-reflect-2.13.5.jar;%APP_LIB_DIR%\com.propensive.magnolia_2.13-0.17.0.jar;%APP_LIB_DIR%\com.chuusai.shapeless_2.13-2.3.4.jar;%APP_LIB_DIR%\org.json4s.json4s-native_2.13-3.6.11.jar;%APP_LIB_DIR%\org.apache.kafka.kafka-clients-6.2.0-ccs.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-core-2.12.2.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-databind-2.12.2.jar;%APP_LIB_DIR%\org.apache.commons.commons-compress-1.20.jar;%APP_LIB_DIR%\org.slf4j.slf4j-api-1.7.30.jar;%APP_LIB_DIR%\io.confluent.kafka-avro-serializer-6.2.0.jar;%APP_LIB_DIR%\io.confluent.kafka-schema-registry-client-6.2.0.jar;%APP_LIB_DIR%\io.confluent.common-utils-6.2.0.jar;%APP_LIB_DIR%\org.apache.kafka.connect-json-2.8.0.jar;%APP_LIB_DIR%\org.rocksdb.rocksdbjni-5.18.4.jar;%APP_LIB_DIR%\org.jline.jline-3.19.0.jar;%APP_LIB_DIR%\net.java.dev.jna.jna-5.3.1.jar;%APP_LIB_DIR%\com.propensive.mercator_2.13-0.2.1.jar;%APP_LIB_DIR%\org.json4s.json4s-core_2.13-3.6.11.jar;%APP_LIB_DIR%\com.github.luben.zstd-jni-1.4.9-1.jar;%APP_LIB_DIR%\org.lz4.lz4-java-1.7.1.jar;%APP_LIB_DIR%\org.xerial.snappy.snappy-java-1.1.8.1.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-annotations-2.12.2.jar;%APP_LIB_DIR%\io.confluent.kafka-schema-serializer-6.2.0.jar;%APP_LIB_DIR%\jakarta.ws.rs.jakarta.ws.rs-api-2.1.6.jar;%APP_LIB_DIR%\org.glassfish.jersey.core.jersey-common-2.34.jar;%APP_LIB_DIR%\io.swagger.swagger-annotations-1.6.2.jar;%APP_LIB_DIR%\org.apache.kafka.connect-api-2.8.0.jar;%APP_LIB_DIR%\com.fasterxml.jackson.datatype.jackson-datatype-jdk8-2.10.5.jar;%APP_LIB_DIR%\org.json4s.json4s-ast_2.13-3.6.11.jar;%APP_LIB_DIR%\org.json4s.json4s-scalap_2.13-3.6.11.jar;%APP_LIB_DIR%\com.thoughtworks.paranamer.paranamer-2.8.jar;%APP_LIB_DIR%\jakarta.annotation.jakarta.annotation-api-1.3.5.jar;%APP_LIB_DIR%\org.glassfish.hk2.external.jakarta.inject-2.6.1.jar;%APP_LIB_DIR%\org.glassfish.hk2.osgi-resource-locator-1.0.3.jar"
set "APP_MAIN_CLASS=ua.edu.ucu.Main"
set "SCRIPT_CONF_FILE=%APP_HOME%\conf\application.ini"

rem Bundled JRE has priority over standard environment variables
if defined BUNDLED_JVM (
  set "_JAVACMD=%BUNDLED_JVM%\bin\java.exe"
) else (
  if "%JAVACMD%" neq "" (
    set "_JAVACMD=%JAVACMD%"
  ) else (
    if "%JAVA_HOME%" neq "" (
      if exist "%JAVA_HOME%\bin\java.exe" set "_JAVACMD=%JAVA_HOME%\bin\java.exe"
    )
  )
)

if "%_JAVACMD%"=="" set _JAVACMD=java

rem Detect if this java is ok to use.
for /F %%j in ('"%_JAVACMD%" -version  2^>^&1') do (
  if %%~j==java set JAVAINSTALLED=1
  if %%~j==openjdk set JAVAINSTALLED=1
)

rem BAT has no logical or, so we do it OLD SCHOOL! Oppan Redmond Style
set JAVAOK=true
if not defined JAVAINSTALLED set JAVAOK=false

if "%JAVAOK%"=="false" (
  echo.
  echo A Java JDK is not installed or can't be found.
  if not "%JAVA_HOME%"=="" (
    echo JAVA_HOME = "%JAVA_HOME%"
  )
  echo.
  echo Please go to
  echo   http://www.oracle.com/technetwork/java/javase/downloads/index.html
  echo and download a valid Java JDK and install before running keyword-detection-service.
  echo.
  echo If you think this message is in error, please check
  echo your environment variables to see if "java.exe" and "javac.exe" are
  echo available via JAVA_HOME or PATH.
  echo.
  if defined DOUBLECLICKED pause
  exit /B 1
)

rem if configuration files exist, prepend their contents to the script arguments so it can be processed by this runner
call :parse_config "%SCRIPT_CONF_FILE%" SCRIPT_CONF_ARGS

call :process_args %SCRIPT_CONF_ARGS% %%*

set _JAVA_OPTS=!_JAVA_OPTS! !_JAVA_PARAMS!

if defined CUSTOM_MAIN_CLASS (
    set MAIN_CLASS=!CUSTOM_MAIN_CLASS!
) else (
    set MAIN_CLASS=!APP_MAIN_CLASS!
)

rem Call the application and pass all arguments unchanged.
"%_JAVACMD%" !_JAVA_OPTS! !KEYWORD_DETECTION_SERVICE_OPTS! -cp "%APP_CLASSPATH%" %MAIN_CLASS% !_APP_ARGS!

@endlocal

exit /B %ERRORLEVEL%


rem Loads a configuration file full of default command line options for this script.
rem First argument is the path to the config file.
rem Second argument is the name of the environment variable to write to.
:parse_config
  set _PARSE_FILE=%~1
  set _PARSE_OUT=
  if exist "%_PARSE_FILE%" (
    FOR /F "tokens=* eol=# usebackq delims=" %%i IN ("%_PARSE_FILE%") DO (
      set _PARSE_OUT=!_PARSE_OUT! %%i
    )
  )
  set %2=!_PARSE_OUT!
exit /B 0


:add_java
  set _JAVA_PARAMS=!_JAVA_PARAMS! %*
exit /B 0


:add_app
  set _APP_ARGS=!_APP_ARGS! %*
exit /B 0


rem Processes incoming arguments and places them in appropriate global variables
:process_args
  :param_loop
  call set _PARAM1=%%1
  set "_TEST_PARAM=%~1"

  if ["!_PARAM1!"]==[""] goto param_afterloop


  rem ignore arguments that do not start with '-'
  if "%_TEST_PARAM:~0,1%"=="-" goto param_java_check
  set _APP_ARGS=!_APP_ARGS! !_PARAM1!
  shift
  goto param_loop

  :param_java_check
  if "!_TEST_PARAM:~0,2!"=="-J" (
    rem strip -J prefix
    set _JAVA_PARAMS=!_JAVA_PARAMS! !_TEST_PARAM:~2!
    shift
    goto param_loop
  )

  if "!_TEST_PARAM:~0,2!"=="-D" (
    rem test if this was double-quoted property "-Dprop=42"
    for /F "delims== tokens=1,*" %%G in ("!_TEST_PARAM!") DO (
      if not ["%%H"] == [""] (
        set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
      ) else if [%2] neq [] (
        rem it was a normal property: -Dprop=42 or -Drop="42"
        call set _PARAM1=%%1=%%2
        set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
        shift
      )
    )
  ) else (
    if "!_TEST_PARAM!"=="-main" (
      call set CUSTOM_MAIN_CLASS=%%2
      shift
    ) else (
      set _APP_ARGS=!_APP_ARGS! !_PARAM1!
    )
  )
  shift
  goto param_loop
  :param_afterloop

exit /B 0
