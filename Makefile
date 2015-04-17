SOURCE_FILES:=`find . -name "*.java"`

all:
	mkdir -p bin
	javac -d bin -cp lib/SMaRt-eds.jar:lib/PVSS.jar:lib/commons-codec-1.5.jar:lib/core-0.1.4.jar:lib/groovy-1.0-JSR-06.jar:lib/netty-3.1.1.GA.jar:lib/slf4j-api-1.5.8.jar:lib/slf4j-jdk14-1.5.8.jar ${SOURCE_FILES}
