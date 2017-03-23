#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
    mvn verify sonar:sonar -DskipTests \
        -Dsonar.host.url=$SONAR_HOST_URL \
        -Dsonar.login=$SONAR_AUTH_TOKEN \
        -Dsonar.branch=$TRAVIS_BRANCH \
        -Dsonar.test.inclusions=src/test/java/de/hpi/unicorn/application/pages/input/generator/**/* \
		    -Dsonar.inclusions=src/main/java/de/hpi/unicorn/application/pages/input/generator/**/* \

else
    mvn verify sonar:sonar -DskipTests \
        -Dsonar.host.url=$SONAR_HOST_URL \
        -Dsonar.login=$SONAR_AUTH_TOKEN \
        -Dsonar.branch=$TRAVIS_BRANCH \
        -Dsonar.analysis.mode=preview \
        -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
        -Dsonar.github.oauth=$GITHUB_ACCESS_TOKEN \
        -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST \
        -Dsonar.test.inclusions=src/test/java/de/hpi/unicorn/application/pages/input/generator/**/* \
        -Dsonar.inclusions=src/main/java/de/hpi/unicorn/application/pages/input/generator/**/* \

fi
