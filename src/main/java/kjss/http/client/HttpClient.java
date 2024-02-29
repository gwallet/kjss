/*
 *    Copyright 2023 Guillaume Wallet <wallet (dot) guillaume (at) gmail (dot) com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package kjss.http.client;

import kjss.http.Http;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handful class to simply query an HTTP server.
 * Useful to query ReST WebServices.
 */
public class HttpClient implements Http.Client {

    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_READ_TIMEOUT    = Duration.ofMillis(500);

    private final URL      baseUrl;
    private final String   encoding        = "UTF-8";
    private final boolean  followRedirects = true;
    private       Duration connectTimeout  = DEFAULT_CONNECT_TIMEOUT;
    private       Duration readTimeout     = DEFAULT_READ_TIMEOUT;

    public HttpClient(URL baseUrl) { this.baseUrl = baseUrl; }

    public Http.Response exchange(Http.Request request) throws IOException {
        HttpURLConnection httpURLConnection = sendRequest(request);
        try {
            return readResponse(httpURLConnection);
        }
        finally {
            httpURLConnection.disconnect();
        }
    }

    @NotNull private HttpURLConnection sendRequest(Http.Request request) throws IOException {
        String path;
        if (request.hasParams()) {
            path = request.params()
                          .flatMap(param -> param.getValue().stream()
                                                 .map(value -> "%s=%s".formatted(encode(param.getKey()), encode(value))))
                          .collect(Collectors.joining("&", request.getPath() + "?", ""));
            if (!request.hasHeader("Content-Type")) {
                request.setContentType(Http.MediaType.APPLICATION_WWW_FORM_URLENCODED);
            }
        }
        else {
            path = request.getPath();
        }
        if (baseUrl.getFile().endsWith("/")) {
            while (path.startsWith("/")) {
                path = path.substring(1);
            }
        }
        else {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
        }

        URL requestUrl = URI.create(baseUrl + path).toURL();
        HttpURLConnection httpURLConnection = (HttpURLConnection) requestUrl.openConnection();
        httpURLConnection.setConnectTimeout((int) connectTimeout.toMillis());
        httpURLConnection.setReadTimeout((int) readTimeout.toMillis());

        httpURLConnection.setRequestMethod(request.getMethod().name());
        httpURLConnection.setInstanceFollowRedirects(followRedirects);

        if (request.hasHeaders()) {
            request.headers().forEach(header -> httpURLConnection.setRequestProperty(header.getKey(), header.getValue()));
        }
        if (request.getBody() != null && !request.getBody().isBlank()) {
            httpURLConnection.setDoOutput(true);
            try (Writer writer = new OutputStreamWriter(httpURLConnection.getOutputStream())) {
                writer.write(request.getBody());
            }
        }
        return httpURLConnection;
    }

    @NotNull private static Http.Response readResponse(HttpURLConnection httpURLConnection) throws IOException {
        Http.Response.Status status = new Http.Response.Status(httpURLConnection.getResponseCode(),
                                                               httpURLConnection.getResponseMessage());
        Http.Response response = new Http.Response().withStatus(status);
        httpURLConnection.getHeaderFields()
                         .forEach((header, values) -> response.setHeader(header, String.join(",", values)));
        try (Reader streamReader = status.is2XXSuccess()
            ? new InputStreamReader(httpURLConnection.getInputStream())
            : new InputStreamReader(httpURLConnection.getErrorStream())) {

            BufferedReader in = new BufferedReader(streamReader);
            List<String> lines = new ArrayList<>();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                lines.add(inputLine);
            }
            response.setBody(String.join("\n", lines));
        }
        return response;
    }

    public HttpClient withConnectTimeout(Duration connectTimeout) {
        setConnectTimeout(connectTimeout);
        return this;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public HttpClient withReadTimeout(Duration readTimeout) {
        setReadTimeout(readTimeout);
        return this;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    private String encode(String string) {
        try {
            return URLEncoder.encode(string, encoding);
        }
        catch (UnsupportedEncodingException cause) {
            throw new RuntimeException(cause);
        }
    }

}

