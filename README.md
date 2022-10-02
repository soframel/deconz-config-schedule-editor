# DeCONZ config Schedule Editor

Simple java code (command line) to edit the configured schedules, through deCONZ REST APIs. 
Can be used for example on thermostats (tested on Danfoss Ally). 

Compatible with deCONZ version 2.18 (not tested with other versions).

## How to use
Create a local.properties file with properties url, key and sensorid. 
Then build and run the app (main class is org.soframel.homeautomation.deconz.DeconzConfigScheduleEditorMain)