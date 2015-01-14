SOURCE=$(shell find ./src -name '*.java')
CLASSES=$(subst .java,.class,$(SOURCE))
SERVELET=/usr/share/maven-repo/org/apache/tomcat/tomcat-servlet-api/3.0/tomcat-servlet-api-3.0.jar

.PHONY: all code clean

all: code

code: $(SOURCE)
	javac -cp $(SERVELET) -sourcepath ./src -d ./classes $^

clean:
	find ./classes -name '*'.class -exec rm -f {} ';'
	rm -rf jar/*.jar
	
jar:
	jar cfm jar/HyProxy.jar manifest.mf -C ./classes .
	
# ensure the next line is always the last line in this file.
# vi:noet

