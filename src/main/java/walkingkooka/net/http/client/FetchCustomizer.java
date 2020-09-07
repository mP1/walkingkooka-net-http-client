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
import javaemul.internal.annotations.GwtIncompatible;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.HttpRequest;

import java.nio.charset.Charset;

/**
 * Unfortunately the making HTTP requests within a JVM and using XMLHttpRequest are not similar at all, each has many
 * differences such as CORS or the ability to use relative urls. This handles the differences in this leaky abstraction.
 */
public interface FetchCustomizer {

    /**
     * Creates a {@link AbsoluteUrl} for the http client.
     */
    @GwtIncompatible
    AbsoluteUrl httpClientUrl(final HttpRequest request);

    /**
     * Creates a {@link Url} for the {@link XMLHttpRequest}.
     */
    Url browserUrl(final HttpRequest request);

    /**
     * This will only be invoked within a JVM to provide a default charset if necessary to convert the request body to text.
     */
    Charset defaultCharset();

    /**
     * This is called prior to the request being sent. Customisation such as the following should be performed here.
     * <ul>
     * <li>{@link java.net.http.HttpClient.Redirect}</li>
     * <li>timeout</li>>
     * <li>{@link java.net.http.HttpClient.Version}</li>
     * </ul>
     */
    @GwtIncompatible
    void prepareHttpClient(final java.net.http.HttpClient.Builder client,
                           final java.net.http.HttpRequest.Builder request);

    /**
     * This method is only called by javascript and allows customisation of the XMLHttpRequest after open but before send.
     * The send method should not be invoked.
     */
    void prepareBrowser(final XMLHttpRequest request);
}
