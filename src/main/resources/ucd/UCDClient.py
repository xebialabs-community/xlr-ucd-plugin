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
        print "Received response: [%s]\n" % system_configuration_response.getResponse()
        return json.loads(system_configuration_response.getResponse())