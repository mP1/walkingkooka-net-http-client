[![Build Status](https://travis-ci.com/mP1/walkingkooka-net-http-client.svg?branch=master)](https://travis-ci.com/mP1/walkingkooka-net-http-client.svg?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/mP1/walkingkooka-net-http-client/badge.svg?branch=master)](https://coveralls.io/github/mP1/walkingkooka-net-http-client?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/mP1/walkingkooka-net-http-client.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-net-http-client/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/mP1/walkingkooka-net-http-client.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-net-http-client/alerts/)
[![J2CL compatible](https://img.shields.io/badge/J2CL-compatible-brightgreen.svg)](https://github.com/mP1/j2cl-central)



A synchronous leaky abstraction that includes both a `java.net.http` and `XmlHttpRequest` implementation, with the goal
to provide a crossplatform implementation with opportunities to tweak and customise in both environment. Because of the
synchronous nature of the XMLHttpRequest it should only be used within a webworker. Usage in the browser UI thread is
deprecated.


In both cases the `HttpRequest` input parameter provides the method, headers and body text for the request, and a `HttpResponse`
is created from the response. However there are parameters unique to both environments that can be setup by calling the
appropriate prepareXXX method.

- void prepareHttpClient(java.net.http.HttpClient.Builder, java.net.http.HttpRequest.Builder), allows redirection, timeouts, cookies etc. 
- void prepareBrowser(XMLHttpRequest) allows setting properties such as `withCredentials`.

There are several limitations with both implementations.

- Multi-part entities are not currently supported in both environments.
- The Javascript environment includes origin limitations, unlike the JVM. 



