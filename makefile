
PY4J_JAR_PATH:=$(shell python -c "import py4j.java_gateway; print py4j.java_gateway.find_jar_path()")

.PHONY: build

build:
	cd Deeva; javac *.java -classpath $(PY4J_JAR_PATH)
