/*
 * Copyright © 2020 Miroslav Pokorny
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test;


import com.google.j2cl.junit.apt.J2clTestInput;
import elemental2.dom.XMLHttpRequest;
import junitprocessor.shaded.com.google.common.annotations.GwtIncompatible;
import org.junit.Test;
import walkingkooka.net.HostAddress;
import walkingkooka.net.IpPort;
import walkingkooka.net.Url;
import walkingkooka.net.UrlCredentials;
import walkingkooka.net.UrlScheme;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.client.FetchCustomizer;
import walkingkooka.net.http.client.FakeFetchCustomizer;
import walkingkooka.net.http.client.HttpClient;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.net.http.server.HttpResponse;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

@J2clTestInput(JunitTest.class)
public class JunitTest {

    @Test
    public void testRequestGetMethod() {
        final HttpResponse response = this.fetch()
                .apply(HttpRequests.get(HttpTransport.UNSECURED, Url.parseRelative("/request-get-method"), HttpProtocolVersion.VERSION_1_0, HttpEntity.EMPTY));

        checkStatus(HttpStatusCode.OK, response);
    }

    @Test
    public void testRequestPostMethod() {
        final HttpResponse response = this.fetch()
                .apply(HttpRequests.post(HttpTransport.UNSECURED, Url.parseRelative("/request-post-method"), HttpProtocolVersion.VERSION_1_0, HttpEntity.EMPTY));

        checkStatus(HttpStatusCode.OK, response);
    }

    @Test
    public void testRequestHeaderContentType() {
        final HttpResponse response = this.fetch()
                .apply(HttpRequests.get(HttpTransport.UNSECURED, Url.parseRelative("/request-header-content-type"), HttpProtocolVersion.VERSION_1_0, HttpEntity.EMPTY.addHeader(HttpHeaderName.CONTENT_TYPE, MediaType.parse("TEXT/PLAIN-testRequestHeaderContentType"))));

        checkStatus(HttpStatusCode.withCode(299), response);
    }

    @Test
    public void testRequestHeaderCustom() {
        final HttpResponse response = this.fetch()
                .apply(HttpRequests.get(HttpTransport.UNSECURED, Url.parseRelative("/request-header-custom"), HttpProtocolVersion.VERSION_1_0, HttpEntity.EMPTY
                        .addHeader(HttpHeaderName.with("X-Custom-Header-2").stringValues(), "testRequestHeaderCustom-value2")
                        .addHeader(HttpHeaderName.with("X-Custom-Header-3").stringValues(), "testRequestHeaderCustom-value3")
                        .addHeader(HttpHeaderName.with("X-Custom-Header-1").stringValues(), "testRequestHeaderCustom-value1")));

        checkStatus(HttpStatusCode.withCode(299), response);
    }

    @Test
    public void testRequestBody() {
        final HttpResponse response = this.fetch()
                .apply(HttpRequests.post(HttpTransport.UNSECURED, Url.parseRelative("/request-body"), HttpProtocolVersion.VERSION_1_0, HttpEntity.EMPTY.setBodyText("testRequestBody")));

        checkStatus(HttpStatusCode.withCode(299), response);
    }

    @Test
    public void testResponseStatusCodeNoContent() {
        final HttpResponse response = this.fetch()
                .apply(HttpRequests.get(HttpTransport.UNSECURED, Url.parseRelative("/response-status-code?statusCode=" + HttpStatusCode.NO_CONTENT), HttpProtocolVersion.VERSION_1_0, HttpEntity.EMPTY));

        checkStatus(HttpStatusCode.NO_CONTENT, response);
    }

    @Test
    public void testResponseStatusCodeNotFound() {
        final HttpResponse response = this.fetch()
                .apply(HttpRequests.get(HttpTransport.UNSECURED, Url.parseRelative("/response-status-code?statusCode=" + HttpStatusCode.NOT_FOUND), HttpProtocolVersion.VERSION_1_0, HttpEntity.EMPTY));

        checkStatus(HttpStatusCode.NOT_FOUND, response);
    }

    @Test
    public void testResponseHeaderContentType() {
        final HttpResponse response = this.fetch()
                .apply(HttpRequests.get(HttpTransport.UNSECURED, Url.parseRelative("/response-header-content-type"), HttpProtocolVersion.VERSION_1_0, HttpEntity.EMPTY));

        checkStatus(HttpStatusCode.OK, response);

        final HttpEntity entity = this.getEntityOrFail(response);
        assertEquals("incorrect CONTENT-TYPE\n" + entity, Optional.of(MediaType.parse("text/plain-123")), HttpHeaderName.CONTENT_TYPE.headerValue(entity));
    }

    @Test
    public void testResponseBody() {
        final HttpResponse response = this.fetch()
                .apply(HttpRequests.post(HttpTransport.UNSECURED, Url.parseRelative("/response-body"), HttpProtocolVersion.VERSION_1_0, HttpEntity.EMPTY.setBodyText("Request-body-123")));

        checkStatus(HttpStatusCode.OK, response);

        final HttpEntity entity = this.getEntityOrFail(response);
        checkBodyText(entity, "Response-BODY-123");
    }

    private Function<HttpRequest, HttpResponse> fetch() {
        return HttpClient.fetch(new FakeFetchCustomizer() {
            @Override
            public Url browserUrl(final HttpRequest request) {
                return request.url()
                        .set(UrlScheme.HTTP, UrlCredentials.NO_CREDENTIALS, HostAddress.with("localhost"), Optional.of(IpPort.with(9999)));
            }

            @GwtIncompatible
            @Override
            public void prepareHttpClient(final java.net.http.HttpClient.Builder client,
                                          final java.net.http.HttpRequest.Builder request) {
                client.version(java.net.http.HttpClient.Version.HTTP_1_1);
                client.followRedirects(java.net.http.HttpClient.Redirect.NEVER);
                client.connectTimeout(Duration.ofMillis(500));
            }

            @Override
            public void prepareBrowser(final XMLHttpRequest request) {
                // do nothing.
            }
        });
    }

    private void checkStatus(final HttpStatusCode statusCode,
                             final HttpResponse response) {
        assertEquals("statusCode\n" + response, statusCode, response.status().map(HttpStatus::value).orElse(HttpStatusCode.withCode(999)));
    }

    private HttpEntity getEntityOrFail(final HttpResponse response) {
        assertEquals("entity\n" + response, 1, response.entities().size());

        return response.entities().get(0);
    }

    private void checkBodyText(final HttpEntity entity,
                               final String bodyText) {
        assertEquals("response.bodyText\n" + entity, bodyText, entity.bodyText());
    }
}
