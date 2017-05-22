#
# THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
# FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
#

from ucd.UCDClientUtil import UCD_Client_Util


verifySsl = not server['disableSslVerification']
ucd_client = UCD_Client_Util.create_ucd_client(server, username, password, verifySsl)

requestId = ucd_client.application_process_request(application, applicationProcess, environment, versions)

