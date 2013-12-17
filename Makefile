SHELL := /bin/bash
PY4J_JAR_PATH:=$(shell python -c "import py4j.java_gateway; print py4j.java_gateway.find_jar_path()")
TOOL_JAR_PATH:=$(shell ./findjava.sh)
CLASS_PATH:="$(PY4J_JAR_PATH):$(TOOL_JAR_PATH)"

.PHONY: all build build_examples deploy setup_deploy install test test_long

all: build build_examples

build:
	test $(TOOL_JAR_PATH) || test -f $(TOOL_JAR_PATH)
	cd deeva; javac *.java -classpath $(CLASS_PATH)
	cd deeva/processor; javac *.java -classpath $(CLASS_PATH)

build_examples:
	 $(MAKE) -C examples

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
	rm -f deeva/*.class
