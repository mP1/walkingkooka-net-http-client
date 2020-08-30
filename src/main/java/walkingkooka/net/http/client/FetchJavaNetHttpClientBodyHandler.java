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

import walkingkooka.ToStringBuilder;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;

import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.charset.Charset;

/**
 * A {@link BodyHandler} that captures the status and uses the content-type to build a {@link BodySubscriber}.
 */
final class FetchJavaNetHttpClientBodyHandler implements BodyHandler<String> {

    static FetchJavaNetHttpClientBodyHandler with(final Charset defaultCharset) {
        return new FetchJavaNetHttpClientBodyHandler(defaultCharset);
    }

    private FetchJavaNetHttpClientBodyHandler(final Charset defaultCharset) {
        super();
        this.defaultCharset = defaultCharset;
    }

    // BodyHandler......................................................................................................

    @Override
    public BodySubscriber<String> apply(final ResponseInfo responseInfo) {
        this.setStatus(responseInfo);

        return BodySubscribers.ofString(this.contentType(responseInfo));
    }

    private void setStatus(final ResponseInfo responseInfo) {
        this.status = responseInfo.statusCode();
    }

    int status;

    /**
     * Extracts the content type from the {@link ResponseInfo}.
     */
    private Charset contentType(final ResponseInfo responseInfo) {
        return responseInfo.headers()
                .firstValue(HttpHeaderName.CONTENT_TYPE.value())
                .map(c -> MediaType.parse(c).contentTypeCharset(this.defaultCharset))
                .orElse(this.defaultCharset);
    }

    private final Charset defaultCharset;

    // Object...........................................................................................................

    @Override
    public String toString() {
        return ToStringBuilder.empty()
                .value(this.status)
                .label(HttpHeaderName.CONTENT_TYPE.value())
                .value(this.defaultCharset)
                .build();
    }
}
