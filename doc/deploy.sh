#!/bin/bash

set -e

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "Pull request detected. Skipping documentation deployment"
  exit 0
fi

SHA=$(git rev-parse --verify HEAD)

echo "Creating build directory hierarchy"
git fetch origin gh-pages:gh-pages
git worktree add _build gh-pages

echo "Building documentation"
sphinx-build -b html . _build/${TRAVIS_BRANCH}

echo "Preparing git for committing"
git config user.name "Travis CI"
git config user.email "travis@travis-ci.com"

echo -n "Checking for changes... "
cd _build
if git diff --quiet; then
  echo "no changes detected"
  exit 0
fi
echo "documentation changed"

echo "Committing changes"
git add -A .
git commit -m "doc: update documentation for ${TRAVIS_BRANCH}@${SHA}"

echo "Decrypting deployment key"
openssl aes-256-cbc -K $encrypted_a7ed42facd4e_key -iv $encrypted_a7ed42facd4e_iv -in deploy_key.enc -out deploy_key -d

echo "Initializing ssh-agent with deployment key"
chmod 600 deploy_key
eval $(ssh-agent -s)
ssh-add deploy_key

echo "Publishing changes"
REPO=$(git config remote.origin.url)
SSH_REPO=${REPO/https:\/\/github.com\//git@github.com:}
git push $SSH_REPO gh-pages
