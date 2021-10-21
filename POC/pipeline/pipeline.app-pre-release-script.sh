#!/bin/bash

##########################################################################################
### DEV ENABLEMENT - JENKINS PIPELINE OFFERING - Version 3.1.0
### Note: THIS SCRIPT FILE IS EXECUTED BEFORE THE STAGED APP IS RELEASED/SWAPPED IN PCF.
###       Use "%BOOST_STAGE_APP_NAME%" to refer to staged app name
###       Use "%BOOST_APP_NAME%" to refer to release app name
###       Ex: cf bind-service "%BOOST_STAGE_APP_NAME%" example-cloud-service
###           cf app "%BOOST_APP_NAME%"
##########################################################################################

# DO NOT MODIFY THIS LINE: use this helper methods for checking if a service exists and enabled through configuration
serviceExists() { services="$services|$1"; if echo "$cfServices" | grep -E "^$1[[:space:]]" > /dev/null; then return 0; fi; return 1; }; services=''; cfServices=$(cf services)
serviceEnabled() { if [[ "$1" = true ]]; then return 0; fi; return 1; }

### BEGIN CUSTOM SHELL COMMANDS BELOW THIS LINE ###



### END CUSTOM SHELL COMMANDS ABOVE THIS LINE ###