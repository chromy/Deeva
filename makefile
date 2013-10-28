
PY4J_JAR_PATH:=$(shell python -c "import py4j.java_gateway; print py4j.java_gateway.find_jar_path()")
TOOL_JAR_PATH:="/Library/Java/JavaVirtualMachines/jdk1.7.0_40.jdk/Contents/Home/lib/tools.jar"
CLASS_PATH:="$(PY4J_JAR_PATH):$(TOOL_JAR_PATH)"

.PHONY: build deploy setup_deploy install test test_long

build:
	cd deeva; javac *.java -classpath $(CLASS_PATH)

setup_deploy:
	test -d .env || virtualenv .env
	source .env/bin/activate; pip install -r requirements.txt
	echo '#! /usr/bin/env bash\nsource .env/bin/activate\n./run_deeva.py "$$@"' > start_deeva
	chmod u+x start_deeva

deploy: setup_deploy build

test:
	nosetests

test_long: test
	lettuce tests

coverage:
	nosetests --with-coverage --cover-package=deeva
	

