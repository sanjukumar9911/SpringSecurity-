#!/bin/bash

##########################################################################################
### DEV ENABLEMENT - JENKINS PIPELINE OFFERING - Version 3.1.0
### Note: THIS SCRIPT FILE IS EXECUTED BEFORE PUSHING THE APP TO PCF.
##########################################################################################

# DO NOT MODIFY THIS LINE: use this helper method for checking if a service exists
serviceMissing() { services="$services|$1"; if echo "$cfServices" | grep -E "^$1[[:space:]]" > /dev/null; then return 1; fi; return 0; }; services=''; cfServices=$(cf services)

serviceEnabled() { if [[ "$1" = true ]]; then return 0; fi; return 1; }

#########################################################
## CREATE SERVICES - TEAM CUSTOMIZATION STARTS HERE
#########################################################




if serviceEnabled "$CONFIGSERVER_ENABLED" && serviceEnabled "$CONFIGSERVER_CONFIGURED" && serviceMissing 'vdcc-config-server' ; then
cf create-service p.config-server standard vdcc-config-server -c "$(cat <<EOF
    {
        "git": {
            "uri": "$GITHUB_CONFIG_REPO_URL",
            "privateKey": "$GITHUB_SSH_KEY",
            "periodic": true
        }
    }
EOF
)"
  
fi

if serviceMissing 'vdcc-rabbit' ; then
    cf create-service p.rabbitmq three-node-3.7 vdcc-rabbit
    echo "A rabbitmq plan with three-node-3.7 that survives upgrades without downtime has been created. A single-node-3.7 plan can be used for non-critical apps."
fi




#########################################################
## TEAM CUSTOMIZATION ENDS HERE (DO NOT MODIFY BELOW)
#########################################################



# DO NOT MODIFY THIS BLOCK: logic *waits* for above services to be ready to use
k=0
echo "Checking readiness for services: $services|"
while true; do
	output=$(cf services | grep -E "^(;$services)[[:space:]]")
	if [[ $((k % 3)) -eq 0 ]]; then echo "-----------"; echo "$output"; fi
	if [[ "$k" -eq 360 ]]; then echo "FAILURE: Service creation exceeded 1 hour timeout limit."; exit 1; fi
	if echo "$output" | grep failed   >/dev/null; then echo "Service FAILURE."; cf services; exit 1; fi
	if echo "$output" | grep progress >/dev/null; then ((k++)); sleep 10; continue; fi
	break
done
