EXAMPLES = helloworld infinite_helloworld simpleloop throws_exception easy_math

.PHONY = make clean

all: make

make:
	for dir in $(EXAMPLES); do \
		cd $$dir; javac -g *.java; cd ..; \
	done

clean:
	for dir in $(EXAMPLES); do \
		cd $$dir; rm *.class; cd ..; \
	done

