#
# THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
# FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
#

import json
from xlrelease.HttpRequest import HttpRequest

class UCD_Client(object):
    def __init__(self, http_connection, username=None, password=None):
        self.http_request = HttpRequest(http_connection, username, password)

    @staticmethod
    def create_client(http_connection, username=None, password=None):
        return UCD_Client(http_connection, username, password)

    def list_system_configuration(self):
        system_configuration_endpoint = "/cli/systemConfiguration"
        system_configuration_response = self.http_request.get(system_configuration_endpoint, contentType='application/json')
        if not system_configuration_response.isSuccessful():
            raise Exception("Failed to get system configuration properties. Server return [%s], with content [%s]" % (system_configuration_response.status, system_configuration_response.response))
        return json.loads(system_configuration_response.getResponse())

    def application_process_request(self, application, application_process, environment, versions):
        application_process_request_endpoint = "/cli/applicationProcessRequest/request"
        body = {'application': application, 'applicationProcess': application_process, 'environment': environment, 'versions': versions}
        print "Sending request: [%s]\n" % json.dumps(body)
        application_process_request_response = self.http_request.put(application_process_request_endpoint, json.dumps(body), contentType='application/json')
        if not application_process_request_response.isSuccessful():
            raise Exception("Failed to execute application process request. Server return [%s], with content [%s]" % (application_process_request_response.status, application_process_request_response.response))
        return json.loads(application_process_request_response.getResponse())["requestId"]