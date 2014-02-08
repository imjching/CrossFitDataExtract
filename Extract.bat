@echo off
@title CrossFit Data Extract
set CLASSPATH=.;dist\Crossfit.jar;dist\opencsv-2.3.jar
java -server data.Crossfit
pause