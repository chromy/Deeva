EXAMPLES = helloworld infinite_helloworld simpleloop

.PHONY = make clean

all: make

make:
	for dir in $(EXAMPLES); do \
		cd $$dir; javac *.java; cd ..; \
	done

clean:
	for dir in $(EXAMPLES); do \
		cd $$dir; rm *.class; cd ..; \
	done
	

