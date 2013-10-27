Deeva
=====

[![Build Status](https://travis-ci.org/chromy/Deeva.png?branch=master)](https://travis-ci.org/chromy/Deeva)

Building
--------
```bash
$ pip install -r requirements.txt
$ make build
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
