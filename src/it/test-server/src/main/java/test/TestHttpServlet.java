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
 */package test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

abstract class TestHttpServlet extends HttpServlet {

    TestHttpServlet() {
        super();
    }

    protected final void service(final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*"); // required so junit-test tests arent blocked etc.
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");

        super.service(request, response);
    }

    final void dumpRequestHeaders(final HttpServletRequest request,
                                  final HttpServletResponse response) throws ServletException, IOException {
        final PrintWriter writer = response.getWriter();

        // printer headers...one per line.
        for (final Enumeration<String> headers = request.getHeaderNames(); headers.hasMoreElements(); ) {
            final String headerName = headers.nextElement();

            for (final Enumeration<String> values = request.getHeaders(headerName); values.hasMoreElements(); ) {
                writer.println(headerName + ": " + values.nextElement());
            }
        }
    }
}
