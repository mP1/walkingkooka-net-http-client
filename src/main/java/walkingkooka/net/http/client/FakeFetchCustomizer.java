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

import java.net.http.HttpClient.Builder;
import java.nio.charset.Charset;

public class FakeFetchCustomizer implements FetchCustomizer {
    @Override
    @GwtIncompatible
    public AbsoluteUrl httpClientUrl(final HttpRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Url browserUrl(final HttpRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Charset defaultCharset() {
        throw new UnsupportedOperationException();
    }

    @Override
    @GwtIncompatible
    public void prepareHttpClient(final Builder client,
                                  final java.net.http.HttpRequest.Builder request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepareBrowser(final XMLHttpRequest request) {
        throw new UnsupportedOperationException();
    }
}