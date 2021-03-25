/**
 * Copyright 2021 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package integration.util;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public final class PluginTestHelper {

    private static final String BASE_URI = "http://localhost:15516/api/v1";
    private static RequestSpecification httpRequest = null;
    private static final String IMPORT_CONFIG = "/config";
    private static final String IMPORT_TEMPLATE = "/templates/import";
    private static final String START_RELEASE_PLUGIN = "/templates/Applications/Release579169b0403444bda10e1eb9ed2d7de7/start";
    private static final String GET_RELEASE_PREFIX = "/releases/";
    private static final String GET_VARIABLES_SUFFIX = "/variableValues";

    // For UCD put requests
    private static final String BASE_URI_UCD = "https://localhost:8443/cli";
    private static RequestSpecification httpRequest_ucd = null;
    private static final String CREATE_COMPONENT_UCD = "/component/create";
    private static final String SET_PROP_UCD = "/component/propValue";
    private static String COMP_ID = null;
    private static String ROLE_ID = null;
    private static String AGENT_NAME = null;
    private static String APP_ID = null;
    private static String GROUP_ID = null;
    private static List<Integer> successCodes = new ArrayList<Integer>(); 
    
    
    private PluginTestHelper() {
        /*
         * Private Constructor will prevent the instantiation of this class directly
         */
    }

    static {
        baseURI = BASE_URI;
        // Configure authentication
        httpRequest = given().auth().preemptive().basic("admin", "admin");
        successCodes.add(200);
        successCodes.add(204);
    }

    public static void initializeXLR() throws Exception{

        // Load ucd server config
       
        try {
            // Load config
            System.out.println("About to parse server-cofig.json");
            JSONObject requestParamsConfig = getRequestParamsFromFile(getResourceFilePath("docker/initialize/data/server-config.json"));
            httpRequest.header("Content-Type", "application/json");
            httpRequest.header("Accept", "application/json");
            httpRequest.body(requestParamsConfig.toJSONString());
            
            // Post server config
            Response response = httpRequest.post(IMPORT_CONFIG);
            if (!successCodes.contains(response.getStatusCode())) {
                System.out.println("Status line, import server was " + response.getStatusLine() + "");
            } else {
                //String responseId = response.jsonPath().getString("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        try {
        
            // Load the template
            JSONObject requestParams = new JSONObject();
            httpRequest.body(requestParams.toJSONString());
            httpRequest.contentType("multipart/form-data");
            httpRequest.multiPart(new File(getResourceFilePath("docker/initialize/data/release-template.json")));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        // Post template
        Response response = httpRequest.post(IMPORT_TEMPLATE);
        if (!successCodes.contains(response.getStatusCode())) {
            System.out.println("Status line, import template was " + response.getStatusLine() + "");
        } else {
            //String postResponseId = response.jsonPath().getString("id");
        }
        
    }

    public static void initializeUCD() throws Exception{
        System.out.println("Starting UCD initialization");

        baseURI = BASE_URI_UCD;
        // Configure authentication
        httpRequest_ucd = given().auth().preemptive().basic("admin", "admin").relaxedHTTPSValidation();
        httpRequest_ucd.header("Content-Type", "application/json");
        httpRequest_ucd.header("Accept", "application/json");

        try {
            // Create Component
            JSONObject requestParamsConfig = getRequestParamsFromFile(getResourceFilePath("docker/initialize/data/ucdConfigFiles/newComponent.json"));
            httpRequest_ucd.body(requestParamsConfig.toJSONString());
            
            Response response = httpRequest_ucd.put(CREATE_COMPONENT_UCD);
            
            if (!successCodes.contains(response.getStatusCode())) {
                System.out.println("Status line, create component was " + response.getStatusLine() + "");
                throw new Exception("UCD Init - Failed to create component");
            } else {
                COMP_ID = response.jsonPath().getString("id");
                ROLE_ID = response.jsonPath().getString("resourceRole.id");
                System.out.println("UCD - create componenet COMP_ID = "+COMP_ID);
                System.out.println("UCD - create componenet ROLE_ID = "+ROLE_ID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        try {
            // Set property
            JSONObject requestParamsConfig = getRequestParamsFromFile(getResourceFilePath("docker/initialize/data/ucdConfigFiles/newPropValue.json"));
            httpRequest_ucd.body(requestParamsConfig.toJSONString());
            
            Response response = httpRequest_ucd.put(SET_PROP_UCD);
            
            if (!successCodes.contains(response.getStatusCode())) {
                System.out.println("Status line, set prop value was " + response.getStatusLine() + ""); 
                throw new Exception("UCD Init - Failed to set property on component");
            } else {
                System.out.println("UCD - set prop value successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        try {
            // get agent name
            JSONObject requestParamsConfig = getRequestParamsFromFile(getResourceFilePath("docker/initialize/data/ucdConfigFiles/newPropValue.json"));
            httpRequest_ucd.body(requestParamsConfig.toJSONString());
            
            Response response = httpRequest_ucd.get("/agentCLI");
            
            if (!successCodes.contains(response.getStatusCode())) {
                System.out.println("Status line, get agent name value was " + response.getStatusLine() + "");
                throw new Exception("UCD Init - Failed to get agent name");
            } else {
                AGENT_NAME = response.jsonPath().getString("[0].name");
                System.out.println("AGENT_NAME = "+AGENT_NAME);
                System.out.println("UCD - get agent name successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        try {
            // set agent
            JSONObject requestParamsConfig = new JSONObject();
            requestParamsConfig.put("artifactAgent", AGENT_NAME);
            httpRequest_ucd.body(requestParamsConfig.toJSONString());
            
            Response response = httpRequest_ucd.put("/systemConfiguration");
            
            if (!successCodes.contains(response.getStatusCode())) {
                System.out.println("Status line, set agent was " + response.getStatusLine() + "");
                throw new Exception("UCD Init - Failed to set agent");
            } else {
                System.out.println("UCD - set agent successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        try {
            // integrate component
            JSONObject requestParamsConfig = new JSONObject();
            requestParamsConfig.put("component", "helloWorld");
            httpRequest_ucd.body(requestParamsConfig.toJSONString());
            
            Response response = httpRequest_ucd.put("/component/integrate");
            
            if (!successCodes.contains(response.getStatusCode())) {
                System.out.println("Status line, integrate " + response.getStatusLine() + "");
                throw new Exception("UCD Init - Failed integrate component");
            } else {
                System.out.println("UCD - integrate component successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        try {
            // create component process
            JSONObject requestParamsConfig = getRequestParamsFromFile(getResourceFilePath("docker/initialize/data/ucdConfigFiles/componentProcess.json"));
            httpRequest_ucd.body(requestParamsConfig.toJSONString());

            
            Response response = httpRequest_ucd.put("/componentProcess/create");
            
            if (!successCodes.contains(response.getStatusCode())) {
                System.out.println("Status line, create component process " + response.getStatusLine() + "");
                throw new Exception("UCD Init - Failed to create component process");
            } else {
                System.out.println("UCD - create component process successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        try {
            // create application
            JSONObject requestParamsConfig = getRequestParamsFromFile(getResourceFilePath("docker/initialize/data/ucdConfigFiles/newApplication.json"));
            httpRequest_ucd.body(requestParamsConfig.toJSONString());

            Response response = httpRequest_ucd.put("/application/create");
            
            if (!successCodes.contains(response.getStatusCode())) {
                System.out.println("Status line, create application " + response.getStatusLine() + "");
                throw new Exception("UCD Init - Failed to create application");
            } else {
                APP_ID = response.jsonPath().getString("id");
                System.out.println("UCD - create application APP_ID = "+APP_ID);
                System.out.println("UCD - create application successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        try {
            // add component to application
            //JSONObject requestParamsConfig = getRequestParamsFromFile(getResourceFilePath("docker/initialize/data/ucdConfigFiles/newApplication.json"));
            httpRequest_ucd.body("");
            
            Response response = httpRequest_ucd.put("/application/addComponentToApp?component=helloWorld&application=hello Application");
            
            if (!successCodes.contains(response.getStatusCode())) {
                System.out.println("Status line, add component to application " + response.getStatusLine() + "");
                throw new Exception("UCD Init - Failed to add component to application");
            } else {
                System.out.println("UCD - add component to application successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        try {
            // create top level group
            JSONObject requestParamsConfig = new JSONObject();
            requestParamsConfig.put("agent", AGENT_NAME);
            requestParamsConfig.put("name", "helloWorld Tutorial");
            requestParamsConfig.put("description","Top Level Group Resource with agent added");
            httpRequest_ucd.body(requestParamsConfig.toJSONString());
            
            Response response = httpRequest_ucd.put("/resource/create");
            
            if (!successCodes.contains(response.getStatusCode())) {
                System.out.println("Status line, create top level group " + response.getStatusLine() + "");
                throw new Exception("UCD Init - Failed create top level group");
            } else {
                GROUP_ID = response.jsonPath().getString("id");
                System.out.println("UCD - create group GROUP_ID = "+GROUP_ID);
                System.out.println("UCD - create top level group successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        try {
            // create environment
            httpRequest_ucd.body("");
            
            Response response = httpRequest_ucd.put("/environment/createEnvironment?application=hello%20Application&name=helloDeploy");
            
            if (!successCodes.contains(response.getStatusCode())) {
                System.out.println("Status line, create environment" + response.getStatusLine() + "");
                System.out.println("Response" + response+ "");
                throw new Exception("UCD Init - Failed to create environment");
            } else {
                System.out.println("UCD - create environment successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        
/*

        JSONObject requestParams = new JSONObject();
        requestParams.put("name", "helloWorld");
        requestParams.put("description", "");


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
        */
        
    }

    public static org.json.JSONObject getPluginReleaseResult() throws Exception{
        org.json.JSONObject releaseResultJSON = null;
        String responseId = "";
        String releaseResultStr = "";
        // Prepare httpRequest, start the release
        JSONObject requestParams = getRequestParams();
        Response response = given().auth().preemptive().basic("admin", "admin")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .body(requestParams.toJSONString())
            .post(START_RELEASE_PLUGIN);

        ///////// retrieve the planned release id.
        if (response.getStatusCode() != 200) {
            System.out.println("Status line, Start release was " + response.getStatusLine() );
        } else {
            responseId = response.jsonPath().getString("id");
            System.out.println("Start release was successful, id = "+ responseId);
        }

        ///////// Get Archived responses
        // Sleep so XLR can finish processing releases
        System.out.println("Pausing for 2 minutes, waiting for release to complete. If most requests fail with 404, consider sleeping longer.");
        Thread.sleep(120000);
        //////////
        response = given().auth().preemptive().basic("admin", "admin")
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .body(requestParams.toJSONString())
        .get(GET_RELEASE_PREFIX + responseId + GET_VARIABLES_SUFFIX);

        if (response.getStatusCode() != 200) {
            System.out.println("Status line for get variables was " + response.getStatusLine() + "");
        } else {
            //releaseResult = response.jsonPath().get("phases[0].tasks[1].comments[0].text").toString();
            releaseResultStr = response.jsonPath().prettyPrint();
            try {
                releaseResultJSON =  new org.json.JSONObject(releaseResultStr);
            } catch (Exception e) {
                System.out.println("FAILED: EXCEPTION: "+e.getMessage());
                e.printStackTrace();
                throw e;
            }        
        }
        return releaseResultJSON;
    }

    /////////////////// Util methods

    public static String getResourceFilePath(String filePath){ 
        // Working with resource files instead of the file system - OS Agnostic 
        //System.out.println("Requested file path = "+filePath);
        String resourcePath = "";
        ClassLoader classLoader = PluginTestHelper.class.getClassLoader();
        try {
            resourcePath = new File (classLoader.getResource(filePath).toURI()).getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("resourcePath = " + resourcePath);
        return resourcePath;
    }

    public static String readFile(String path) throws IOException {
        StringBuilder result = new StringBuilder("");

        File file = new File(path);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        return result.toString();
    }

    public static JSONObject getRequestParamsFromFile(String filePath) throws Exception{
        JSONObject requestParams = new JSONObject();

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
         
        try (FileReader reader = new FileReader(filePath))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
 
            requestParams = (JSONObject) obj;
            //System.out.println(requestParams);
 
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return requestParams;
    }

    public static JSONObject getRequestParams() {
        // must use intermediate parameterized HashMap to avoid warnings
        HashMap<String,Object> params = new HashMap<String,Object>();
        
        params.put("releaseTitle", "release from api");
        JSONObject requestParams = new JSONObject(params);
        return requestParams;
    }

}