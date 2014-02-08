@echo off
@title CrossFit Data Extract
set CLASSPATH=.;libs\Crossfit.jar;libs\opencsv-2.3.jar
java -server data.Crossfit
pause