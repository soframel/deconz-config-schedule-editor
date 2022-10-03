# Thermostat HTTP Binding for OpenHab -> deconz
For features that are not available through the exposed channels on deconz binding. 
Features supported for now: only schedule_on switch. 

Binding of type HTTP (doc: https://www.openhab.org/addons/bindings/http/)

NOTE: Requires transformation add-ons JSONPATH and JINJA, to be installed in OpenHab add-ons page -> others -> transformations add-ons. 

URL = $deconz/api/<key>/sensors/<id>

## Schedule Switch channel

type= switch
onValue=true
offValue=false
stateTransformation=JSONPATH:$.config.schedule_on
commandMethod=PUT
contentType=application/json
commandTransformation='JINJA:{"config": {"schedule_on": {{value}} }}'

## OpenHab  Yaml
````
label: Thermostat 1 Schedule
thingTypeUID: http:url
configuration:
  authMode: BASIC
  ignoreSSLErrors: true
  baseURL: http://MYHOST:8091/api/MYKEY/sensors/9
  password: none
  delay: 0
  stateMethod: GET
  refresh: 60
  commandMethod: PUT
  contentType: application/json
  timeout: 3000
  bufferSize: 2048
  username: none
channels:
  - id: switch
    channelTypeUID: http:switch
    label: switch
    description: ""
    configuration:
      onValue: "true"      
      offValue: "false"
      stateTransformation: JSONPATH:$.config.schedule_on
      commandTransformation: 'JINJA:{"config": {"schedule_on": {{value}} }}'
````