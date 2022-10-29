# DeCONZ config Schedule Editor

Simple web application to edit weekly schedules through deCONZ REST APIs. 
Can be used for example on thermostats (tested on Danfoss Ally thermostats). 

Compatible with deCONZ version 2.18 (not tested with other versions).

Made with Quarkus and Qute. Requires a JDK 11.

## How to use
Create a .env file with deCONZ URL and API Key + thermostats list: see `example.env`. 

Then run the app. 
Locally: 
`mvn quarkus:dev` 

Once deployed:
`java -jar quarkus-run.jar`

By default uses port 8090.