#! /usr/bin/env bash

set -e

git checkout gh-pages
rm -rf *
git checkout master docs
git reset HEAD
cd docs
make html
cd ..
mv -fv docs/_build/html/* .
rm -rf docs
touch .nojekyll
git add -A
git commit -m "Generated gh-pages for `git log master -1 --pretty=short --abbrev-commit`" && git push origin gh-pages ; git checkout master

