@echo off
@title CrossFit Data Extract
set CLASSPATH=.;libs\*
java -server data.Crossfit
pause