@echo off
echo "installing epp, make sure java and maven are installed correctly and their respective executables are set in your PATH environment variable or setup will fail..."
echo "PATH is set to: %PATH%"
set MVN_OPTS="-Dfile.encoding=UTF-8 -Xmx512M"
jar xf jbpt.zip	>epp.log
if %errorlevel% EQU 0 (
	echo "JBPT extracted"
) else (
	echo "jar command failed, see epp.log ... EXIT"
	goto :EOF
)
echo "installing developer environment, this may take a while..."
echo "1/10"
call mvn -f Esper/pom.xml clean install -DskipTests >>epp.log
findstr "FAILED" epp.log
if %errorlevel% GEQ 1 (
	echo "OK"
) else (
	echo "command failed, see epp.log ... EXIT"
	goto :EOF
)
echo "2/10"
call mvn -f jbpt/pom.xml clean install -DskipTests >>epp.log
findstr "FAILED" epp.log
if %errorlevel% GEQ 1 (
	echo "OK"
) else (
	echo "command failed, see epp.log ... EXIT"
	goto :EOF
)
echo "3/10"
call mvn -f jbpt/jbpt-core/pom.xml clean install -DskipTests >>epp.log
findstr "FAILED" epp.log
if %errorlevel% GEQ 1 (
	echo "OK"
) else (
	echo "command failed, see epp.log ... EXIT"
	goto :EOF
)
echo "4/10"
call mvn -f jbpt/jbpt-deco/pom.xml clean install -DskipTests >>epp.log
findstr "FAILED" epp.log
if %errorlevel% GEQ 1 (
	echo "OK"
) else (
	echo "command failed, see epp.log ... EXIT"
	goto :EOF
)
echo "5/10"
call mvn -f EapCommons/pom.xml clean install -DskipTests >>epp.log
findstr "FAILED" epp.log
if %errorlevel% GEQ 1 (
	echo "OK"
) else (
	echo "command failed, see epp.log ... EXIT"
	goto :EOF
)
echo "6/10"
call mvn -f EapImport/pom.xml clean install -DskipTests >>epp.log
findstr "FAILED" epp.log
if %errorlevel% GEQ 1 (
	echo "OK"
) else (
	echo "command failed, see epp.log ... EXIT"
	goto :EOF
)
echo "7/10"
call mvn -f EapSemantic/pom.xml clean install -DskipTests >>epp.log
findstr "FAILED" epp.log
if %errorlevel% GEQ 1 (
	echo "OK"
) else (
	echo "command failed, see epp.log ... EXIT"
	goto :EOF
)
echo "8/10"
call mvn -f EapEventProcessing/pom.xml clean install -DskipTests >>epp.log
findstr "FAILED" epp.log
if %errorlevel% GEQ 1 (
	echo "OK"
) else (
	echo "command failed, see epp.log ... EXIT"
	goto :EOF
)
echo "9/10"
call mvn -f EapSimulation/pom.xml clean install -DskipTests >>epp.log
findstr "FAILED" epp.log
if %errorlevel% GEQ 1 (
	echo "OK"
) else (
	echo "command failed, see epp.log ... EXIT"
	goto :EOF
)
echo "10/10"
call mvn -f EapWebInterface/pom.xml clean install -DskipTests >>epp.log
findstr "FAILED" epp.log
if %errorlevel% GEQ 1 (
	echo "OK"
) else (
	echo "command failed, see epp.log ... EXIT"
	goto :EOF
)
rd /s/q jbpt
echo "done, restart script at any time to recompile your projects, this may also be done directly from eclipse"