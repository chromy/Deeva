Deeva
=====

[![Build Status](https://magnum.travis-ci.com/chromy/Deeva.png?token=m7fgCEW8zGb4ttyG4Loj&branch=master)](https://magnum.travis-ci.com/chromy/Deeva)

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
