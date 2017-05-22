#
# THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
# FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
#

import urllib

from xlrelease import HttpResponse

from java.lang import String

from org.apache.commons.codec.binary import Base64
from org.apache.http import HttpHost
from org.apache.http.client.config import RequestConfig
from org.apache.http.util import EntityUtils
from org.apache.http.client.methods import HttpGet, HttpPost, HttpPut, HttpDelete
from org.apache.http.entity import StringEntity
from org.apache.http.impl.client import HttpClients
from org.apache.http.conn.ssl import SSLContextBuilder, SSLConnectionSocketFactory, TrustStrategy

class TrustAllStrategy(TrustStrategy):
    def isTrusted(self, chain, authType):
        return True

class HttpRequest:
    def __init__(self, username = None, password = None, verify = True):
        """
        Builds an HttpRequest

        :param username: the username for basic authentication
            (optional, no authentication will be used if empty)
        :param password: an password
            (optional)
        """
        self.username = username
        self.password = password
        self.verify   = verify

    def doRequest(self, **options):
        """
        Performs an HTTP Request

        :param options: A keyword arguments object with the following properties :
            method: the HTTP method : 'GET', 'PUT', 'POST', 'DELETE'
                (optional: GET will be used if empty)
            context: the context url
                (optional: the url on HttpConnection will be used if empty)
            body: the body of the HTTP request for PUT & POST calls
                (optional: an empty body will be used if empty)
            contentType: the content type to use
                (optional, no content type will be used if empty)
            headers: a dictionary of headers key/values
                (optional, no headers will be used if empty)
        :return: an HttpResponse instance
        """
        request = self.buildRequest(
            options.get('method', 'GET'),
            options.get('context', ''),
            options.get('body', ''),
            options.get('contentType', None),
            options.get('headers', None))

        return self.executeRequest(request)


    def get(self, context, **options):
        """
        Performs an Http GET Request

        :param context: the context url
        :param options: the options keyword argument described in doRequest()
        :return: an HttpResponse instance
        """
        options['method'] = 'GET'
        options['context'] = context
        return self.doRequest(**options)


    def put(self, context, body, **options):
        """
        Performs an Http PUT Request

        :param context: the context url
        :param body: the body of the HTTP request
        :param options: the options keyword argument described in doRequest()
        :return: an HttpResponse instance
        """
        options['method'] = 'PUT'
        options['context'] = context
        options['body'] = body
        return self.doRequest(**options)


    def post(self, context, body, **options):
        """
        Performs an Http POST Request

        :param context: the context url
        :param body: the body of the HTTP request
        :param options: the options keyword argument described in doRequest()
        :return: an HttpResponse instance
        """
        options['method'] = 'POST'
        options['context'] = context
        options['body'] = body
        return self.doRequest(**options)


    def delete(self, context, **options):
        """
        Performs an Http DELETE Request

        :param context: the context url
        :param options: the options keyword argument described in doRequest()
        :return: an HttpResponse instance
        """
        options['method'] = 'DELETE'
        options['context'] = context
        return self.doRequest(**options)


    def buildRequest(self, method, context, body, contentType, headers):

        method = method.upper()

        if method == 'GET':
            request = HttpGet(context)
        elif method == 'POST':
            request = HttpPost(context)
            request.setEntity(StringEntity(body))
        elif method == 'PUT':
            request = HttpPut(context)
            request.setEntity(StringEntity(body))
        elif method == 'DELETE':
            request = HttpDelete(context)
        else:
            raise Exception('Unsupported method: ' + method)

        request.addHeader('Content-Type', contentType)
        request.addHeader('Accept', contentType)
        self.setCredentials(request)
        self.setHeaders(request, headers)

        return request


    def setCredentials(self, request):
        if self.username:
            username = self.username
            password = self.password
        else:
            return

        encoding = Base64.encodeBase64String(String(username + ':' + password).getBytes())
        request.addHeader('Authorization', 'Basic ' + encoding)


    def setHeaders(self, request, headers):
        if headers:
            for key in headers:
                request.setHeader(key, headers[key])


    def executeRequest(self, request):
        client = None
        response = None
        try:
            if (self.verify):
                client = HttpClients.createDefault()
            else:
                client = self.createHttpClient()

            response = client.execute(request)
            status = response.getStatusLine().getStatusCode()
            entity = response.getEntity()
            result = EntityUtils.toString(entity, "UTF-8") if entity else None
            headers = response.getAllHeaders()
            EntityUtils.consume(entity)

            return HttpResponse.HttpResponse(status, result, headers)
        finally:
            if response:
                response.close()
            if client:
                client.close()

    
    def createHttpClient(self):
        builder = SSLContextBuilder()
        builder.loadTrustMaterial(None, TrustAllStrategy())
      
        tlsVersions = ["TLSv1", "TLSv1.1", "TLSv1.2"]
        socketfactory = SSLConnectionSocketFactory(builder.build(), tlsVersions, None, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
        # print 'DEBUG: Created custom HttpClient to trust all certs\n'
        return HttpClients.custom().setSSLSocketFactory(socketfactory).build()
