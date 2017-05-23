#
# Copyright 2017 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

import time

from ucd.UCDClientUtil import UCD_Client_Util

verify_ssl = not server['disableSslVerification']
ucd_client = UCD_Client_Util.create_ucd_client(server, username, password, verify_ssl)
trial = 0
request_response = None
while not numberOfPollingTrials or trial < numberOfPollingTrials:
    trial += 1
    time.sleep(pollingInterval)
    request_response = ucd_client.application_process_request_status(requestId)
    requestStatus = request_response["status"]
    requestResult = request_response["result"]
    print "Received Request Status: [%s] with Request Result: [%s]\n" % (requestStatus, requestResult)
    if requestStatus in ("CLOSED", "FAULTED"):
        if requestResult not in "SUCCEEDED":
            raise Exception("Failed to execute application process request. Status [%s], Result [%s]" % (
            requestStatus, requestResult))
        break
