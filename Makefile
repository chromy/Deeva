SHELL := /bin/bash
PY4J_JAR_PATH:=$(shell python -c "import py4j.java_gateway; print py4j.java_gateway.find_jar_path()")
TOOL_JAR_PATH:=$(shell ./findjava.sh)
GSON_JAR_PATH:= "lib/gson-2.2.4.jar"
CLASS_PATH:="$(PY4J_JAR_PATH):$(TOOL_JAR_PATH):$(GSON_JAR_PATH)"
SOURCE_FILE:= $(shell mktemp -t sources.XXXXXX)
EXAMPLE_SOURCE_FILE:= $(shell mktemp -t classes.XXXXXX)


.PHONY: all build build_examples deploy setup_deploy install test test_long examples clean_examples clean clean_all

all: build build_examples

build: clean
	test $(TOOL_JAR_PATH) || test -f $(TOOL_JAR_PATH)
	find deeva -name "*.java" > $(SOURCE_FILE)
	javac -g @$(SOURCE_FILE) -classpath $(CLASS_PATH)

examples: build_examples

build_examples: clean_examples
	find examples -name "*.java" > $(SOURCE_FILE)
	javac -g @$(SOURCE_FILE)

clean_examples:
	find examples -name "*.class" -print0 | xargs -0 rm -f

setup_deploy:
	test -d .env || virtualenv .env
	source .env/bin/activate; pip install -r requirements.txt;
	source .env/bin/activate; cd deeva; javac *.java -classpath $(CLASS_PATH)
	echo '#!/bin/bash' > start_deeva
	echo 'DIR=$$(dirname "$$(readlink -f "$$0")")' >> start_deeva
	echo 'source $$DIR/.env/bin/activate' >> start_deeva
	echo '$$DIR/run_deeva.py $$@' >> start_deeva
	chmod u+x start_deeva

deploy: setup_deploy

test:
	nosetests

test_long: test
	lettuce tests

coverage:
	nosetests --with-coverage --cover-package=deeva

clean:
	find deeva -name "*.class" -print0 | xargs -0 rm -f
	find . -name "*.pyc" -print0 | xargs -0 rm -f

clean_all: clean clean_examples
	echo "Everything cleaned"
