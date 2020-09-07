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

import elemental2.dom.XMLHttpRequest;
import walkingkooka.Cast;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.HasHeaders;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;

import java.util.List;
import java.util.Map.Entry;

// https://github.com/gwtproject/gwt-http/blob/master/src/main/java/org/gwtproject/http/client/RequestBuilder.java
final class FetchElemental2XmlHttpRequest extends Fetch {

    static FetchElemental2XmlHttpRequest with(final FetchCustomizer customizer) {
        return new FetchElemental2XmlHttpRequest(customizer);
    }

    private FetchElemental2XmlHttpRequest(final FetchCustomizer customizer) {
        super(customizer);
    }

    @Override
    HttpResponse apply0(final HttpRequest request,
                        final FetchCustomizer customizer) {
        final XMLHttpRequest xmlHttpRequest = new XMLHttpRequest();
        try {
            xmlHttpRequest.open(request.method().value(),
                    customizer.browserUrl(request).value(),
                    ASYNC);

            // add request headers...
            for (final Entry<HttpHeaderName<?>, List<?>> header : request.headers().entrySet()) {
                final HttpHeaderName<?> headerName = header.getKey();
                final String headerNameString = headerName.value();
                for (final Object headerValue : header.getValue()) {
                    xmlHttpRequest.setRequestHeader(headerNameString, headerName.headerText(Cast.to(headerValue)));
                }
            }

            customizer.prepareBrowser(xmlHttpRequest);

            xmlHttpRequest.send(request.bodyText());

            final HttpStatus status = HttpStatusCode.withCode(xmlHttpRequest.status)
                    .setMessageOrDefault(xmlHttpRequest.statusText);

            return response(status, parseHeaders(xmlHttpRequest.getAllResponseHeaders())
                    .setBodyText(xmlHttpRequest.responseText));
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Throwable e) {
            throw new FetchException(e.getMessage(), e);
        }
    }

    /**
     * Always FALSE, this function always makes synchronous requests.
     */
    private final static boolean ASYNC = false;

    /**
     * Accepts a single string holding all headers and adds all the individual header values.
     */
    static HttpEntity parseHeaders(final String headers) {
        HttpEntity entity = HttpEntity.EMPTY;

        for (final String headerLine : headers.split(HasHeaders.LINE_ENDING.toString())) {
            final int colon = headerLine.indexOf(':');
            if (-1 == colon) {
                continue; // ignore bad response headers
            }

            final HttpHeaderName<?> headerName = HttpHeaderName.with(headerLine.substring(0, colon).trim());
            entity = entity.addHeader(headerName, Cast.to(headerName.parse(headerLine.substring(colon + 1).trim())));
        }

        return entity;
    }

    @Override
    public String toString() {
        return XMLHttpRequest.class.getSimpleName();
    }
}
