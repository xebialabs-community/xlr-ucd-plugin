#
# Copyright 2017 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#


import json
from ucd.HttpRequest import HttpRequest

class UCD_Client(object):
    def __init__(self, http_connection, username=None, password=None, verify = True):
        self.http_request = HttpRequest(http_connection, username, password, verify)

    @staticmethod
    def create_client(http_connection, username=None, password=None, verify = True):
        return UCD_Client(http_connection, username, password, verify)

    def list_system_configuration(self):
        system_configuration_endpoint = "/cli/systemConfiguration"
        system_configuration_response = self.http_request.get(system_configuration_endpoint, contentType='application/json')
        if not system_configuration_response.isSuccessful():
            raise Exception("Failed to get system configuration properties. Server return [%s], with content [%s]" % (system_configuration_response.status, system_configuration_response.response))
        return json.loads(system_configuration_response.getResponse())

    def application_process_request(self, application, application_process, environment, versions, properties):
        application_process_request_endpoint = "/cli/applicationProcessRequest/request"
        versions_list = []
        for component,version in versions.iteritems():
            versions_list.append({'version':version, 'component':component})
        body = {'application': application, 'applicationProcess': application_process, 'environment': environment, 'properties': properties, 'versions': versions_list}
        print "Sending request: [%s]\n" % json.dumps(body)
        application_process_request_response = self.http_request.put(application_process_request_endpoint, json.dumps(body), contentType='application/json')
        if not application_process_request_response.isSuccessful():
            raise Exception("Failed to execute application process request. Server return [%s], with content [%s]" % (application_process_request_response.status, application_process_request_response.response))
        return json.loads(application_process_request_response.getResponse())["requestId"]

    def application_process_request_status(self, request_id):
        application_process_request_status_endpoint = "/cli/applicationProcessRequest/requestStatus?request=%s" % request_id
        application_process_request_status_response = self.http_request.get(application_process_request_status_endpoint, contentType='application/json')
        if not application_process_request_status_response.isSuccessful():
            raise Exception("Failed to get status application process request. Server return [%s], with content [%s]" % (application_process_request_status_response.status, application_process_request_status_response.response))
        return json.loads(application_process_request_status_response.getResponse())
