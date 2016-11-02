#
# THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
# FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
#
import time, sys
from ucd.UCDClientUtil import UCD_Client_Util


ucd_client = UCD_Client_Util.create_ucd_client(server, username, password)
trial = 0
request_status = None
request_response = None
while not numberOfPollingTrials or trial < numberOfPollingTrials:
    trial += 1
    time.sleep(pollingInterval)
    request_response = ucd_client.application_process_request_status(requestId)
    requestStatus = request_response["status"]
    requestResult = request_response["result"]
    if requestStatus in ("CLOSED", "FAULTED"):
        if requestResult not in "SUCCEEDED":
            raise Exception("Failed to execute application process request. Status [%s], Result [%s]" % (requestStatus, requestResult))




