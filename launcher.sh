#!/bin/bash
javac -d classes -cp classes *.java
java -cp classes mouserun.GameStarter 20 20 50 120