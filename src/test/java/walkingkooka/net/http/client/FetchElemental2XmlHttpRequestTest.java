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

import org.junit.jupiter.api.Test;
import walkingkooka.net.header.ContentEncoding;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;

public final class
FetchElemental2XmlHttpRequestTest extends FetchTestCase<FetchElemental2XmlHttpRequest> {

    @Test
    public void testParseHeaders() {
        this.parseHeadersAndCheck("Content-type: text/plain",
                HttpEntity.EMPTY.addHeader(HttpHeaderName.CONTENT_TYPE, MediaType.TEXT_PLAIN));
    }

    @Test
    public void testParseHeadersMultiple() {
        this.parseHeadersAndCheck("Content-type: text/plain\r\nContent-Length: 123",
                HttpEntity.EMPTY.addHeader(HttpHeaderName.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                        .addHeader(HttpHeaderName.CONTENT_LENGTH, 123L));
    }

    @Test
    public void testParseHeadersMultiple2() {
        this.parseHeadersAndCheck("Content-type: text/plain\r\nContent-Length: 123\r\n",
                HttpEntity.EMPTY.addHeader(HttpHeaderName.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                        .addHeader(HttpHeaderName.CONTENT_LENGTH, 123L));
    }

    @Test
    public void testParseHeadersMultiple3() {
        this.parseHeadersAndCheck("Content-type: text/plain\r\nContent-Length: 123\r\nServer: Server456",
                HttpEntity.EMPTY.addHeader(HttpHeaderName.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                        .addHeader(HttpHeaderName.CONTENT_LENGTH, 123L)
                        .addHeader(HttpHeaderName.SERVER, "Server456"));
    }

    @Test
    public void testParseHeadersDuplicateHeader() {
        this.parseHeadersAndCheck("Content-Encoding: gzip\r\nContent-Encoding: compress",
                HttpEntity.EMPTY.addHeader(HttpHeaderName.CONTENT_ENCODING, ContentEncoding.GZIP)
                        .addHeader(HttpHeaderName.CONTENT_ENCODING, ContentEncoding.COMPRESS));
    }

    @Test
    public void testParseHeadersDuplicateHeader2() {
        this.parseHeadersAndCheck("Content-Encoding: gzip\r\nContent-Length: 123\r\nContent-Encoding: compress",
                HttpEntity.EMPTY.addHeader(HttpHeaderName.CONTENT_ENCODING, ContentEncoding.GZIP)
                        .addHeader(HttpHeaderName.CONTENT_LENGTH, 123L)
                        .addHeader(HttpHeaderName.CONTENT_ENCODING, ContentEncoding.COMPRESS));
    }

    private void parseHeadersAndCheck(final String headerText,
                                      final HttpEntity expected) {
        this.checkEquals(expected,
                FetchElemental2XmlHttpRequest.parseHeaders(headerText),
                () -> "parseHeaders\n" + headerText + "\nfailed");
    }

    @Override
    public Class<FetchElemental2XmlHttpRequest> type() {
        return FetchElemental2XmlHttpRequest.class;
    }
}
