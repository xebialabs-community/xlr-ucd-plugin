# xlr-ucd-plugin

This plugin offers an interface from XL Release to Urban Code Deploy Server. 

## CI status ##

[![Build Status][xlr-ucd-plugin-travis-image]][xlr-ucd-plugin-travis-url]
[![Codacy Badge][xlr-ucd-plugin-codacy-image] ][xlr-ucd-plugin-codacy-url]
[![Code Climate][xlr-ucd-plugin-code-climate-image] ][xlr-ucd-plugin-code-climate-url]

[xlr-ucd-plugin-travis-image]: https://travis-ci.org/xebialabs-community/xlr-ucd-plugin.svg?branch=master
[xlr-ucd-plugin-travis-url]: https://travis-ci.org/xebialabs-community/xlr-ucd-plugin
[xlr-ucd-plugin-codacy-image]: https://api.codacy.com/project/badge/Grade/da9c2f00342c40ad8efc7fbd1aaec063
[xlr-ucd-plugin-codacy-url]: https://www.codacy.com/app/joris-dewinne/xlr-ucd-plugin
[xlr-ucd-plugin-code-climate-image]: https://codeclimate.com/github/xebialabs-community/xlr-ucd-plugin/badges/gpa.svg
[xlr-ucd-plugin-code-climate-url]: https://codeclimate.com/github/xebialabs-community/xlr-ucd-plugin

## Development ##

* Start XLR: `./gradlew runDocker`
* Start UCD: 
  ```
  docker run -p 8443:8443 -p 8082:8080 -e "LICENSE=accept" 
      -v $(pwd)/src/test/resources/ucd/server.xml:/opt/ibm-ucd/server/opt/tomcat/conf/server.xml 
      -v $(pwd)/src/test/resources/ucd/installed.properties:/opt/ibm-ucd/server/conf/server/installed.properties 
      --name UCD ibmcom/urbancode-deploy
  ```
  
## References ##
+ [UCD REST api](http://www.ibm.com/support/knowledgecenter/SS4GSP_6.2.3/com.ibm.udeploy.reference.doc/topics/rest_api_ref_commands.html)


