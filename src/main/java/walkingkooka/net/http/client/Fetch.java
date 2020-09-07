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


import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;

import java.util.Objects;
import java.util.function.Function;

/**
 * Base class that defines the two fetcher implementations.
 */
abstract class Fetch implements Function<HttpRequest, HttpResponse> {

    Fetch(final FetchCustomizer customizer) {
        super();
        this.customizer = customizer;
    }

    @Override
    public final HttpResponse apply(final HttpRequest request) {
        Objects.requireNonNull(request, "request");

        return this.apply0(request, this.customizer);
    }

    private final FetchCustomizer customizer;

    abstract HttpResponse apply0(final HttpRequest request, final FetchCustomizer customizer);

    /**
     * Helper that creates a {@link HttpResponse} using the provided status and entity
     */
    static HttpResponse response(final HttpStatus status,
                                 final HttpEntity entity) {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(status);
        response.addEntity(entity);
        return response;
    }
}
