#!/bin/sh
#
# Copyright 2021 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#


SCRIPT=$(readlink -f "$0")
# Absolute path this script is in, thus /home/user/bin
SCRIPTPATH=$(dirname "$SCRIPT")

####################### UCD server data

curl --user admin:admin -i -X POST http://localhost:15516/api/v1/config \
  -H "Content-Type: application/json" -H "Accept: application/json" \
  --data "@"$SCRIPTPATH"/data/server-config.json"

########### LOAD XLR TEMPLATE

curl --user admin:admin -i -X POST http://localhost:15516/api/v1/templates/import \
    -H "Content-Type: application/json" -H "Accept: application/json" \
    --data "@"$SCRIPTPATH"/data/release-template.json"

########### POPULATE UCD with scenario data
# Curl cmd 1 create component
RESPONSE1=$(curl -k -u admin:admin "https://localhost:8443/cli/component/create" -X PUT -d "@"$SCRIPTPATH"/data/ucdConfigFiles/newComponent.json")
COMPID=$(echo $RESPONSE1 | jq -r '.id')
echo $COMPID
ROLEID=$(echo $RESPONSE1 | jq -r '.resourceRole.id')
echo $ROLEID

# Curl cmd 2 set a component property
RESPONSE2=$(curl -k -u admin:admin "https://localhost:8443/cli/component/propValue" -X PUT -d "@"$SCRIPTPATH"/data/ucdConfigFiles/newPropValue.json")

# Curl cmd 3 get agent name, set agent, then integrate
RESPONSE3=$(curl -k -u admin:admin "https://localhost:8443/cli/agentCLI")
echo "RESPONSE3 = $RESPONSE3"
AGENTNAME=$(echo $RESPONSE3 | jq -r '.[0] | .name')
echo $AGENTNAME
curl -k -u admin:admin https://localhost:8443/cli/systemConfiguration -X PUT -d {"artifactAgent":"$AGENTNAME"}
curl -k -u admin:admin https://localhost:8443/cli/component/integrate -X PUT -d {"component":"helloWorld"}

# Curl cmd 4 create component process
RESPONSE4=$(curl -k -u admin:admin https://localhost:8443/cli/componentProcess/create -X PUT -d "@"$SCRIPTPATH"/data/ucdConfigFiles/componentProcess.json")
echo "RESPONSE4 = $RESPONSE4"
# Curl cmd 5 create application
RESPONSE5=$(curl -k -u admin:admin "https://localhost:8443/cli/application/create" -X PUT -d "@"$SCRIPTPATH"/data/ucdConfigFiles/newApplication.json")
echo "RESPONSE5 = $RESPONSE5"
APPID=$(echo $RESPONSE5 | jq -r '.id')
# Curl cmd 6 add component to application
RESPONSE6=$(curl -k -u admin:admin "https://localhost:8443/cli/application/addComponentToApp?component=helloWorld&application=hello%20Application" -X PUT)
echo "RESPONSE6 = $RESPONSE6"
# Curl cmd 7 create top level group
RESPONSE7=$(curl -k -u admin:admin "https://localhost:8443/cli/resource/create" -X PUT -H "Content-Type: application/json" -d '{"agent":"'$AGENTNAME'", "name":"helloWorld Tutorial", "description":"Top Level Group Resource with agent added"}')
echo "RESPONSE7 = $RESPONSE7"
GROUPID=$(echo $RESPONSE7 | jq -r '.id')
echo "GROUPID = $GROUPID"
# Curl cmd 8 create environment
RESPONSE8=$(curl -k -u admin:admin "https://localhost:8443/cli/environment/createEnvironment?application=hello%20Application&name=helloDeploy" -X PUT)
echo "RESPONSE8 = $RESPONSE8"
# Curl cmd 9 associate top level resource with environment
RESPONSE9=$(curl -k -u admin:admin "https://localhost:8443/cli/environment/addBaseResource?application=hello%20Application&environment=helloDeploy&resource=%2FhelloWorld%20Tutorial" -X PUT)
echo "RESPONSE9 = $RESPONSE9"
# Curl cmd 10 create component resource with mappings
MODJSON='{"roleId":'$ROLEID', "name":"helloWorld", "description":"Mapping Component to Environment Resource", "inheritTeam":"true", "useImpersonation":"false", "roleProperties":{}, "parentId":'$GROUPID',"teamMappings":[]}'
echo "MODJSON = $MODJSON"
RESPONSE10=$(curl -k -u admin:admin "https://localhost:8443/cli/resource/create" -X PUT -H "Content-Type: application/json" -d "$MODJSON")
RESOURCEID=$(echo $RESPONSE10 | jq -r '.id')
echo "RESOURCEID = $RESOURCEID"
# Curl cmd 11 add tag to resource
curl -k -u admin:admin "https://localhost:8443/cli/resource/tag?resource="$RESOURCEID"&tag=blueCycle&color=0000FF" -X PUT
# Curl cmd 12 create application process
APROCESS=$(jq --arg app $APPID '.application = $app' $SCRIPTPATH/data/ucdConfigFiles/applicationProcess.json)
#echo "APROCESS = $APROCESS"
RESPONSE12=$(curl -k -u admin:admin "https://localhost:8443/cli/applicationProcess/create" -H "Content-Type: application/json" -X PUT -d "$APROCESS")
echo "RESPONSE12 = $RESPONSE12"
echo "Demo has been initialized"










