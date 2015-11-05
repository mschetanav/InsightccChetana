#!/usr/bin/env bash

#The code is built on Java 8.0, but it should work on 6+.

#For exercise 1, when a tweet line does not have the tweet text or timestamp, the output is skipped. 

java -cp lib/json-simple-1.1.1.jar:lib/CodingChallengeChetana.jar TweetCleaner

#For exercise 2, we are still printing the average degree (same output as previous).

java -cp lib/json-simple-1.1.1.jar:lib/CodingChallengeChetana.jar AvgDegreeCalculator


