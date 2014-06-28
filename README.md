Deeva
=====

[![Build Status](https://travis-ci.org/chromy/Deeva.png?branch=master)](https://travis-ci.org/chromy/Deeva)

Deploy
------
```bash
$ git clone https://github.com/chromy/Deeva.git
$ make deploy
$ ln -s start_deeva [somewhere on your path]/deeva
```

Building
--------
```bash
$ pip install -r requirements.txt
$ make build
```

Debugging a program
--------
In order to debug any program, you need to compile java source code with "-g flag"
e.g. 
```bash
$ javac -g linkedlistdemo/*.java
```
To start debugging, run the command "./run_deeva.py" with java class which contains main method e.g.
```bash
$ ./run_deeva.py linkedlistdemo.LinkedList
```

Running Tests
-------------
You will need to get the [Chrome driver](http://chromedriver.storage.googleapis.com/index.html?path=2.4/)
and put it somewhere on your path.

```bash
$ make test       # Fast tests
$ make test_long  # Fast then slow tests
```

To run the tests by hand you can do:

```bash
$ nosetests       # Python unit tests
$ lettuce tests   # System tests
```

To Generate Documentation
-------------------------
```bash
$ pip install sphinx
$ cd docs
$ make html
```
