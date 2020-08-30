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

abstract class FetchSelector2 extends FetchSelector {

    /**
     * This will disappear during the transpile phase leaving {@link FetchSelector#fetch(FetchCustomizer)} actually being used in javascript.
     */
    @GwtIncompatible
    static Fetch fetch(final FetchCustomizer customizer) {
        return FetchJavaNetHttpClient.with(customizer);
    }
}
