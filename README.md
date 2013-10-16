Deeva
=====

[![Build Status](https://travis-ci.org/chromy/Deeva.png?token=XXSWJz9i5a1KH8iRHhzr&branch=master)](https://magnum.travis-ci.com/chromy/Deeva)

Installation
------------
For OSX cry :(

Building
--------
```bash
$ pip install -r requirements.txt
```

Running Tests
-------------
You will need to get the [Chrome driver](http://chromedriver.storage.googleapis.com/index.html?path=2.4/)
and put it somewhere on your path.

```bash
$ nosetests
$ lettuce tests
```

To Generate Documentation
-------------------------
```bash
$ pip install sphinx
$ cd docs
$ make html
```
