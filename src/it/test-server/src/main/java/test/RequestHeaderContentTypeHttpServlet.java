/*
 * Copyright © 2020 Miroslav Pokorny
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
 */
package test;

import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.HttpStatusCode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class RequestHeaderContentTypeHttpServlet extends TestHttpServlet {

    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response) throws ServletException, IOException {
        final String contentType = request.getHeader(HttpHeaderName.CONTENT_TYPE.value());
        if ("text/plain-testRequestHeaderContentType".equals(contentType)) {
            response.setStatus(299, "OK!");
        } else {
            this.log("Incorrect content-type: " + contentType);

            response.setStatus(HttpStatusCode.BAD_REQUEST.code(), "Invalid Content-Type");

            this.dumpRequestHeaders(request, response);
        }
    }
}