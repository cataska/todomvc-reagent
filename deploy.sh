#!/bin/bash
set -e

: ${upstream:=origin}
: ${REPO:=https://github.com/cataska/todomvc-reagent.git}

git fetch $upstream
if [ `git rev-list HEAD...$upstream/master --count` -ne 0 ]; then
  echo "not deploying"
  exit 1
fi

lein release

rm -fr _public
git clone $REPO -b gh-pages _public

REV=`git describe --always`
cp public/js/app.js _public/js/app.js
cd _public

git add js/app.js
git commit -m "regen for $REV"
git push origin gh-pages

cd ..

