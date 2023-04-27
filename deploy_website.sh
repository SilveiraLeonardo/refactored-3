
#!/bin/bash

set -ex

REPO="git@github.com:square/okhttp.git"
DIR="temp-clone"

rm -rf "$DIR"

git clone "$REPO" "$DIR"

cd "$DIR"

./gradlew dokkaHtmlMultiModule

mv ./build/dokka/htmlMultiModule docs/4.x

cat README.md | grep -v 'project website' > docs/index.md
cp CHANGELOG.md docs/changelogs/changelog.md
cp CONTRIBUTING.md docs/contribute/contributing.md

mkdocs gh-deploy

git checkout gh-pages
git cherry-pick bb229b9dcc9a21a73edbf8d936bea88f52e0a3ff
git cherry-pick c695732f1d4aea103b826876c077fbfea630e244
git push --set-upstream origin gh-pages

cd ..
rm -rf "$DIR"
