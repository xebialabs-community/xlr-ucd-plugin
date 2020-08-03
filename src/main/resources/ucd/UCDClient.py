#
# Copyright 2020 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#


import json
import time

from ucd.HttpRequest import HttpRequest


def check_response(response, message):
    if not response.isSuccessful():
        raise Exception(message)


class UCD_Client(object):
    def __init__(self, http_connection, username=None, password=None, verify=True):
        self.http_request = HttpRequest(http_connection, username, password, verify)

    @staticmethod
    def create_client(http_connection, username=None, password=None, verify=True):
        return UCD_Client(http_connection, username, password, verify)

    def ucd_listsystemconfiguration(self, variables):
        system_configuration_endpoint = "/cli/systemConfiguration"
        system_configuration_response = self.http_request.get(system_configuration_endpoint,
                                                              contentType='application/json')
        check_response(system_configuration_response,
                            "Failed to get system configuration properties. Server return [%s], with content [%s]" % (
                                system_configuration_response.status, system_configuration_response.response))
        result = json.loads(system_configuration_response.getResponse())
        variables['systemConfiguration'] = result
        return result

    def ucd_applicationprocessrequest(self, variables):
        application_process_request_endpoint = "/cli/applicationProcessRequest/request"
        versions_list = []
        for component, version in variables['versions'].iteritems():
            versions_list.append({'version': version, 'component': component})
        body = {'application': variables['application'], 'applicationProcess': variables['applicationProcess'],
                'environment': variables['environment'], 'properties': variables['properties'],
                'versions': versions_list}
        print "Sending request: [%s]\n" % json.dumps(body)
        application_process_request_response = self.http_request.put(application_process_request_endpoint,
                                                                     json.dumps(body), contentType='application/json')
        check_response(application_process_request_response,
                            "Failed to execute application process request. Server return [%s], with content [%s]" % (
                                application_process_request_response.status,
                                application_process_request_response.response))
        result = json.loads(application_process_request_response.getResponse())["requestId"]
        variables['requestId'] = result
        return result

    def application_process_request_status(self, request_id):
        application_process_request_status_endpoint = "/cli/applicationProcessRequest/requestStatus?request=%s" % request_id
        application_process_request_status_response = self.http_request.get(application_process_request_status_endpoint,
                                                                            contentType='application/json')
        check_response(application_process_request_status_response,
                            "Failed to get status application process request. Server return [%s], with content [%s]" % (
                                application_process_request_status_response.status,
                                application_process_request_status_response.response))
        return json.loads(application_process_request_status_response.getResponse())

    def ucd_applicationprocessrequeststatus(self, variables):
        trial = 0
        while not variables['numberOfPollingTrials'] or trial < variables['numberOfPollingTrials']:
            trial += 1
            time.sleep(variables['pollingInterval'])
            request_response = self.application_process_request_status(variables['requestId'])
            variables['requestStatus'] = request_response["status"]
            variables['requestResult'] = request_response["result"]
            print "Received Request Status: [%s] with Request Result: [%s]\n" % (
                variables['requestStatus'], variables['requestResult'])
            if variables['requestStatus'] in ("CLOSED", "FAULTED"):
                if variables['requestResult'] not in "SUCCEEDED":
                    raise Exception("Failed to execute application process request. Status [%s], Result [%s]" % (
                        variables['requestStatus'], variables['requestResult']))
                break

    def ucd_synchronousapplicationprocessrequest(self, variables):
        self.ucd_applicationprocessrequest(variables)
        self.ucd_applicationprocessrequeststatus(variables)
