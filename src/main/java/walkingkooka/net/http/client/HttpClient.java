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

import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.reflect.PublicStaticHelper;

import java.util.function.Function;

public final class HttpClient implements PublicStaticHelper {

    /**
     * {@see Fetch}
     */
    public static Function<HttpRequest, HttpResponse> fetch(final FetchCustomizer customizer) {
        return FetchSelector2.fetch(customizer);
    }

    /**
     * Stop creation
     */
    private HttpClient() {
        throw new UnsupportedOperationException();
    }
}
