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

import javaemul.internal.annotations.GwtIncompatible;
import walkingkooka.Cast;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * A {@link Fetch} that uses {@link walkingkooka.net.http.client.HttpClient}.
 */
final class FetchJavaNetHttpClient extends Fetch {

    @GwtIncompatible
    static FetchJavaNetHttpClient with(final FetchCustomizer customizer) {
        return new FetchJavaNetHttpClient(customizer);
    }

    private FetchJavaNetHttpClient(final FetchCustomizer customizer) {
        super(customizer);
    }

    @Override
    HttpResponse apply0(final HttpRequest request,
                        final FetchCustomizer customizer) {
        try {
            final Charset defaultCharset = customizer.defaultCharset();

            final java.net.http.HttpRequest.Builder requestBuilder = java.net.http.HttpRequest.newBuilder(uri(request, customizer));
            requestBuilder.method(request.method().value(),
                    bodyPublisher(request, defaultCharset));

            // add request headers
            request.headers().forEach((h, v) -> {
                final String headerName = h.value();
                for (final Object value : v) {
                    requestBuilder.setHeader(headerName, h.headerText(Cast.to(value)));
                }
            });

            final HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
            customizer.prepareHttpClient(httpClientBuilder, requestBuilder);

            final FetchJavaNetHttpClientBodyHandler bodyHandler = FetchJavaNetHttpClientBodyHandler.with(defaultCharset);

            final java.net.http.HttpResponse<String> javaNetHttpHttpResponse = httpClientBuilder.build()
                    .send(requestBuilder.build(), bodyHandler);

            // capture response headers
            HttpEntity entity = HttpEntity.EMPTY;
            for (final Entry<String, List<String>> headerAndValues : javaNetHttpHttpResponse.headers().map().entrySet()) {
                final HttpHeaderName<?> header = HttpHeaderName.with(headerAndValues.getKey());
                entity = entity.setHeader(header,
                        Cast.to(headerAndValues.getValue().stream().map(header::parse).collect(Collectors.toList())));
            }
            return response(HttpStatusCode.withCode(bodyHandler.status).status(), entity.setBodyText(javaNetHttpHttpResponse.body()));
        } catch (final IOException | InterruptedException cause) {
            throw new FetchException(cause.getMessage(), cause);
        }
    }

    /**
     * Returns a {@link URI} from the request.
     */
    private static URI uri(final HttpRequest request, final FetchCustomizer customizer) {
        return URI.create(customizer.httpClientUrl(request).value());
    }

    /**
     * Returns a {@link BodyPublisher} with the given {@link String body}.
     */
    private static BodyPublisher bodyPublisher(final HttpRequest request,
                                               final Charset defaultCharset) {
        final Charset charset = HttpHeaderName.CONTENT_TYPE.headerValue(request)
                .map(m -> m.contentTypeCharset(defaultCharset))
                .orElse(defaultCharset);

        return BodyPublishers.ofString(request.bodyText(), charset);
    }

    @Override
    public String toString() {
        return HttpClient.class.getName();
    }
}
