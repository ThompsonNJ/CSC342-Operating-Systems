SOURCES=src/osp/*/*.java
CLASSES=bin/osp/*/*.class

OPTS=

all: build

build: $(CLASSES)
	@(test -d bin/osp/tmp || \
		(mkdir -p bin/osp/tmp; make) \
		|| (echo "Something wrong: Can't create the directory for the *.class files"; exit 1))


$(CLASSES): $(SOURCES) 
	javac -g -classpath .:bin/:$(CLASSPATH):OSP.jar -d bin/ $(SOURCES)

run: 	build
	java -classpath .:bin/:$(CLASSPATH):OSP.jar osp.OSP -noGUI $(OPTS)

gui: 	build
	java -classpath .:bin/:$(CLASSPATH):OSP.jar osp.OSP $(OPTS)

demo:
	java -classpath .:bin/:$(CLASSPATH):Demo.jar osp.OSP $(OPTS)

debug: 	build
	jdb -classpath .:bin/:$(CLASSPATH):OSP.jar osp.OSP $(OPTS)

clean:
	/bin/rm -rf bin/osp temp *.log saved
