
PY4J_JAR_PATH:=$(shell python -c "import py4j.java_gateway; print py4j.java_gateway.find_jar_path()")

.PHONY: build deploy setup_deploy install

build:
	cd deeva; javac *.java -classpath $(PY4J_JAR_PATH)

setup_deploy:
	test -d .env || virtualenv .env
	source .env/bin/activate; pip -r requirements.txt
	echo "#! /usr/bin/env bash\nsource .env/bin/activate\n./run_deeva.py" > start_deeva
	chmod u+x start_deeva

deploy: setup_deploy build

