EXAMPLES = helloworld infinite_helloworld simpleloop runtime_exception functions easy_math multiple_classes demo stdin

.PHONY = make clean

all: make

make:
	for dir in $(EXAMPLES); do \
		cd $$dir; javac -g -cp ../../ *.java; cd ..; \
	done

clean:
	for dir in $(EXAMPLES); do \
		cd $$dir; rm -f *.class; cd ..; \
	done
