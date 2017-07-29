#!/usr/bin/env bash

./gradlew build && docker build --rm -t rzd . && docker run -p 8080:8080 rzd