/*
 * Copyright 2020 Miroslav Pokorny (github.com/mP1)
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
 *
 */

package walkingkooka.net.http.client;

import org.junit.After;
import org.junit.jupiter.api.Test;
import walkingkooka.Binary;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.HostAddress;
import walkingkooka.net.IpPort;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.UrlCredentials;
import walkingkooka.net.UrlScheme;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.net.http.server.jetty.JettyHttpServer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class FetchJavaNetHttpClientTest extends FetchTestCase<FetchJavaNetHttpClient> {

    private final static HostAddress SERVER = HostAddress.with("localhost");

    @Test
    public void testRequestTimeout() throws Exception {
        try (final ServerSocket serverSocket = new ServerSocket(IpPort.free().value())) {

            this.server((req, resp) -> {
                throw new Error();
            });

            final FetchException thrown = assertThrows(FetchException.class,
                    () -> this.fetch(IpPort.free())
                            .apply(this.request(HttpMethod.GET, Url.parseRelative("/does-not-exist"), HttpEntity.EMPTY)));
            assertEquals("Connection refused", thrown.getMessage());
        }
    }

    @Test
    public void testGetRequest() {
        final HttpMethod method = HttpMethod.GET;
        final RelativeUrl url = Url.parseRelative("/resource1");
        final HttpHeaderName<MediaType> contentTypeHeader = HttpHeaderName.CONTENT_TYPE;
        final MediaType contentTypeValueIn = MediaType.parse("text/plain;x=1");

        final MediaType contentTypeValueOut = MediaType.parse("text/plain;y=2");
        final HttpStatus status = HttpStatusCode.OK.setMessage("OK Message 123");
        final String bodyText = "BodyText123456";

        final IpPort port = this.server((req, resp) -> {
            assertEquals(method, req.method(), () -> "method\n" + req);
            assertEquals(url, req.url(), () -> "url\n" + req);
            assertEquals(Optional.of(contentTypeValueIn),
                    contentTypeHeader.header(req),
                    () -> "incorrect header " + contentTypeHeader + "\n" + req);

            resp.setStatus(status);
            resp.addEntity(HttpEntity.EMPTY
                    .addHeader(contentTypeHeader, contentTypeValueOut)
                    .setBodyText(bodyText));
        });

        final FetchJavaNetHttpClient fetch = this.fetch(port);
        this.checkResponse(fetch.apply(this.request(method,
                url,
                HttpEntity.EMPTY
                        .addHeader(contentTypeHeader, contentTypeValueIn))),
                status,
                Maps.of(contentTypeHeader, list(contentTypeValueOut)),
                bodyText);
    }

    @Test
    public void testPostRequestWithBody() {
        final HttpMethod method = HttpMethod.POST;
        final RelativeUrl url = Url.parseRelative("/resource1");
        final HttpHeaderName<MediaType> contentTypeHeader = HttpHeaderName.CONTENT_TYPE;
        final MediaType contentTypeValueIn = MediaType.parse("text/plain;x=1");
        final String bodyTextIn = "BodyText123456-IN";

        final MediaType contentTypeValueOut = MediaType.parse("text/plain;y=2");
        final HttpStatus status = HttpStatusCode.OK.setMessage("OK Message 123");
        final String bodyTextOut = "BodyText123456-OUT";

        final IpPort port = this.server((req, resp) -> {
            assertEquals(method, req.method(), () -> "method\n" + req);
            assertEquals(url, req.url(), () -> "url\n" + req);
            assertEquals(Optional.of(contentTypeValueIn),
                    contentTypeHeader.header(req),
                    () -> "incorrect header " + contentTypeHeader + "\n" + req);
            assertEquals(bodyTextIn,
                    req.bodyText(),
                    () -> "bad request body\n" + req);

            resp.setStatus(status);
            resp.addEntity(HttpEntity.EMPTY
                    .addHeader(contentTypeHeader, contentTypeValueOut)
                    .setBodyText(bodyTextOut));
        });

        final FetchJavaNetHttpClient fetch = this.fetch(port);
        this.checkResponse(fetch.apply(this.request(method,
                url,
                HttpEntity.EMPTY
                        .addHeader(contentTypeHeader, contentTypeValueIn)
                        .setBodyText(bodyTextIn))),
                status,
                Maps.of(contentTypeHeader, list(contentTypeValueOut)),
                bodyTextOut);
    }

    private HttpRequest request(final HttpMethod method,
                                final RelativeUrl url,
                                final HttpEntity entity) {
        return HttpRequests.value(method,
                HttpTransport.UNSECURED,
                url,
                HttpProtocolVersion.VERSION_1_0,
                entity);
    }

    private IpPort server(final BiConsumer<HttpRequest, HttpResponse> handler) {
        assertNull(this.server, "server from previous wasnt shutdown");

        final IpPort port = IpPort.free();
        this.server = JettyHttpServer.with(SERVER,
                port,
                (req, resp) -> {
                    try {
                        handler.accept(req, resp);
                    } catch (final Throwable cause) {
                        cause.printStackTrace();

                        resp.setStatus(HttpStatusCode.INTERNAL_SERVER_ERROR.setMessage("Assertion Failed"));

                        final StringWriter body = new StringWriter();
                        final PrintWriter writer = new PrintWriter(body);
                        cause.printStackTrace(writer);
                        writer.flush();

                        final byte[] bodyText = body.toString().getBytes(StandardCharsets.UTF_8);

                        resp.addEntity(HttpEntity.EMPTY
                                .addHeader(HttpHeaderName.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                                .addHeader(HttpHeaderName.CONTENT_LENGTH, Long.valueOf(bodyText.length))
                                .setBody(Binary.with(bodyText))
                        );
                    }
                });
        this.server.start();
        return port;
    }

    @After
    public void shutdownServerIfNecessary() {
        HttpServer server = this.server;
        if (null != server) {
            this.server = null;
            server.stop();
        }
    }

    private HttpServer server;

    private FetchJavaNetHttpClient fetch(final IpPort port) {
        return FetchJavaNetHttpClient.with(new FakeFetchCustomizer() {
            @Override
            public AbsoluteUrl httpClientUrl(final HttpRequest request) {
                return request.url()
                        .set(UrlScheme.HTTP, UrlCredentials.NO_CREDENTIALS, SERVER, Optional.of(port));
            }

            @Override
            public Charset defaultCharset() {
                return Charset.defaultCharset();
            }

            @Override
            public void prepareHttpClient(final HttpClient.Builder client,
                                          final java.net.http.HttpRequest.Builder request) {
                client.version(Version.HTTP_1_1);
                client.connectTimeout(Duration.ofMillis(500));
                client.followRedirects(Redirect.NEVER);
                request.timeout(Duration.ofMillis(500));
            }
        });
    }

    private void checkResponse(final HttpResponse response,
                               final HttpStatus status,
                               final Map<HttpHeaderName<?>, List<Object>> requiredHeaders,
                               final String bodyText) {
        assertEquals(Optional.of(status.value()), response.status().map(HttpStatus::value), "status\n" + response);

        final List<HttpEntity> entities = response.entities();
        assertEquals(1, entities.size(), "1 entity\n" + response);

        final HttpEntity first = entities.get(0);
        final Map<HttpHeaderName<?>, List<?>> responseHeaders = first.headers();

        final Map<HttpHeaderName<?>, List<?>> filtered = Maps.ordered();
        requiredHeaders.keySet().forEach(h -> {
            final List<?> values = responseHeaders.get(h);
            if (null != values) {
                filtered.put(h, values);
            }
        });

        assertEquals(requiredHeaders, filtered, () -> "response headers incorrect\n" + response.toString());
        assertEquals(bodyText, first.bodyText(), () -> "response body\n" + response.toString());
    }

    private static <T> List<T> list(final T items) {
        return Lists.of(items);
    }

    @Override
    public Class<FetchJavaNetHttpClient> type() {
        return FetchJavaNetHttpClient.class;
    }
}
