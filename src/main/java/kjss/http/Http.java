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

package kjss.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Basic HTTP protocol used to query HTTP server.
 */
public final class Http {

    /**
     * HTTP only consists of request/response exchange.
     */
    public interface Client {

        Http.Response exchange(Http.Request request) throws IOException;

    }

    /**
     * @param path the path relative to the root URL of the server.
     * @return Build and return an HTTP OPTIONS request. Used as preflight request with CORS.
     * @see <a href="https://developer.mozilla.org/en-US/docs/Glossary/Preflight_request">preflight request</a>.
     * @see <a href="https://developer.mozilla.org/en-US/docs/Glossary/CORS">CORS</a>.
     */
    public static Request options(String path) {
        return new Request(Request.Method.OPTIONS, path);
    }

    /**
     * @param path the path relative to the root URL of the server.
     * @return Build and return an HTTP GET request.
     */
    public static Request get(String path) {
        return new Request(Request.Method.GET, path);
    }

    /**
     * @param path the path relative to the root URL of the server.
     * @return Build and return an HTTP POST request.
     */
    public static Request post(String path) {
        return new Request(Request.Method.POST, path);
    }

    /**
     * HTTP Request to be sent to an HTTP server.
     */
    public static final class Request extends Http.Message<Http.Request> {

        /**
         * Useful set of request headers.
         */
        public interface Headers extends Http.Message.Headers {

            String ACCEPT                         = "Accept";
            String ACCEPT_LANGUAGE                = "Accept-Language";
            String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
            String AUTHORIZATION                  = "Authorization";
            String CACHE_CONTROL                  = "Cache-Control";
            String COOKIE                         = "Cookie";
            String EXPIRES                        = "Expires";
            String IF_MODIFIED_SINCE              = "If-Modified-Since";
            String IF_UNMODIFIED_SINCE            = "If-Unmodified-Since";
            String IF_MATCH                       = "If-Match";
            String IF_NONE_MATCH                  = "If-None-Match";
            String ORIGIN                         = "Origin";

        }

        public enum Method {
            CONNECT,
            DELETE,
            GET,
            HEAD,
            OPTIONS,
            PATCH,
            POST,
            PUT,
            TRACE
        }

        private final Method                    method;
        private final String                    path;
        private final Map<String, List<String>> params = new LinkedHashMap<>();

        private Request(Method method, String path) {
            this.method = method;
            this.path   = path;
        }

        public Method getMethod() {
            return method;
        }

        public String getPath() {
            return path;
        }

        public Stream<Map.Entry<String, List<String>>> params() {
            return params.entrySet().stream();
        }

        public boolean hasParams() {
            return !params.isEmpty();
        }

        public Request accept(MediaType mediaType) {
            return withHeader(Http.Request.Headers.ACCEPT, mediaType.toString());
        }

        public Request withParam(String param, String... values) {
            setParam(param, values);
            return this;
        }

        public void setParam(String param, String... values) {
            List<String> queryParamValues = params.getOrDefault(param, new ArrayList<>());
            params.put(param, queryParamValues);
            queryParamValues.addAll(Arrays.asList(values));
        }

    }

    /**
     * HTTP response sent by an HTTP server once it has handle the request previously sent.
     */
    public static final class Response extends Http.Message<Http.Response> {

        /**
         * Useful set of HTTP response headers.
         */
        public interface Headers extends Http.Message.Headers {

            String ACCESS_CONTROL_ALLOW_ORIGIN      = "Access-Control-Allow-Origin";
            String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
            String ACCESS_CONTROL_ALLOW_HEADERS     = "Access-Control-Allow-Headers";
            String ACCESS_CONTROL_ALLOW_METHODS     = "Access-Control-Allow-Methods";
            String ACCESS_CONTROL_EXPOSE_HEADERS    = "Access-Control-Expose-Headers";
            String ACCESS_CONTROL_MAX_AGE           = "Access-Control-Max-Age";
            String ETAG                             = "ETag";
            String LAST_MODIFIED                    = "Last-Modified";
            String SET_COOKIE                       = "Set-Cookie";
            String TIMING_ALLOW_ORIGIN              = "Timing-Allow-Origin";

        }

        /**
         * The server sends status code and status message alongside the response.
         */
        public record Status (
            int code,
            String message
        ) {
            //  Usual status codes
            //  2xx
            public static final Status OK                     = new Status(200, "OK");
            public static final Status CREATED                = new Status(201, "Created");
            public static final Status ACCEPTED               = new Status(202, "Accepted");
            //  3xx
            public static final Status MOVED_PERMANENTLY      = new Status(301, "Moved Permanently");
            public static final Status FOUND                  = new Status(302, "Found");
            public static final Status NOT_MODIFIED           = new Status(304, "Not Modified");
            public static final Status TEMPORARY_REDIRECT     = new Status(307, "Temporary Redirect");
            public static final Status PERMANENT_REDIRECT     = new Status(308, "Permanent Redirect");
            //  4xx
            public static final Status BAD_REQUEST            = new Status(400, "Bad Request");
            public static final Status UNAUTHORIZED           = new Status(401, "Unauthorized");
            public static final Status FORBIDDEN              = new Status(401, "Forbidden");
            public static final Status NOT_FOUND              = new Status(404, "Not Found");
            public static final Status METHOD_NOT_ALLOWED     = new Status(405, "Method Not Allowed");
            public static final Status NOT_ACCEPTABLE         = new Status(406, "Not Acceptable");
            public static final Status CONFLICT               = new Status(409, "Conflict");
            public static final Status UNSUPPORTED_MEDIA_TYPE = new Status(415, "Unsupported Media Type");
            //  5xx
            public static final Status INTERNAL_SERVER_ERROR  = new Status(500, "Internal Server Error");
            public static final Status BAD_GATEWAY            = new Status(502, "Bad Gateway");
            public static final Status SERVICE_UNAVAILABLE    = new Status(503, "Service Unavailable");

            public boolean is2XXSuccess() {
                return 200 <= code && code < 300;
            }

            public boolean is4XXClientSideError() {
                return 400 <= code && code < 500;
            }

            public boolean is5XXServerSideError() {
                return 500 <= code && code < 600;
            }

            @Override public boolean equals(Object obj) {
                if (! ( obj instanceof Status ))
                    return false;

                return Objects.equals(((Status)obj).code, code);
            }

            @Override public String toString() {
                return format("%d - %s", code, message);
            }

        }

        private Status status;

        public Status getStatus() {
            return status;
        }

        public Response withStatus(Status status) {
            setStatus(status);
            return this;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        @Override public String toString() {
            StringBuilder buffer = new StringBuilder()
                .append(status == null ? "NO STATUS CODE" : status.code)
                .append(" ")
                .append(status == null ? "NO STATUS MESSAGE" :status.message)
                .append("\n\n")
                .append(body);
            return buffer.toString();
        }

    }

    /**
     * Media type consists of {@code type/subtype}, or {@code type/subtype;parameter=value}.
     */
    public record MediaType (
        String type,
        String subtype,
        String parameterName,
        String parameterValue
    ) {

        //  Useful set of Media Types

        public static final MediaType APPLICATION_JSON                = MediaType.of("application", "json");
        public static final MediaType APPLICATION_OCTET_STREAM        = MediaType.of("application", "octet-stream");
        public static final MediaType TEXT_HTML                       = MediaType.of("text", "html");
        public static final MediaType TEXT_PLAIN                      = MediaType.of("text", "plain");
        public static final MediaType TEXT_XML                        = MediaType.of("text", "xml");
        public static final MediaType APPLICATION_WWW_FORM_URLENCODED = MediaType.of("application", "x-www-form-urlencoded");

        @Override public boolean equals(Object obj) {
            if (obj instanceof MediaType other) {
                return Objects.equals(type, other.type)
                       && Objects.equals(subtype, other.subtype);
            }
            return false;
        }

        @Override public String toString() {
            if (parameterName == null || parameterName.isBlank()) {
                return "%s/%s".formatted(type, subtype);
            }
            else {
                return "%s/%s; %s=%s".formatted(type, subtype, parameterName, parameterValue);
            }
        }

        public MediaType withoutParameter() {
            return MediaType.of(type, subtype);
        }

        public MediaType withParameter(String parameter, String value) {
            return MediaType.of(type, subtype, parameter, value);
        }

        public static MediaType fromString(String string) {
            String[] mediaType_parameter = string.split(";");
            String[] type_subtype = mediaType_parameter[0].split("/");
            String type = type_subtype[0].trim();
            String subtype = type_subtype[1].trim();
            if (mediaType_parameter.length < 2) {
                return of(type, subtype);
            }
            else {
                String[] parameter_value = mediaType_parameter[1].split("=");
                return of(type, subtype, parameter_value[0], parameter_value[1]);
            }
        }

        public static MediaType of(String type, String subtype) {
            return MediaType.of(type, subtype, null, null);
        }

        public static MediaType of(String type, String subtype, String parameterName, String parameterValue) {
            return new MediaType(type, subtype, parameterName, parameterValue);
        }

    }

    protected static abstract class Message<M extends Message<M>> {

        /**
         * Useful set of Request / Response headers
         */
        protected interface Headers {

            String CONTENT_TYPE = "Content-Type";

        }

        protected final Map<String, String> headers = new LinkedHashMap<>();
        protected       String              body;

        public Stream<Map.Entry<String, String>> headers() {
            return headers.entrySet().stream();
        }

        public boolean hasHeaders() {
            return !headers.isEmpty();
        }

        public boolean hasHeader(String header) {
            return headers.containsKey(header);
        }

        public String getHeader(String header) {
            return headers.get(header);
        }

        @SuppressWarnings("unchecked")
        public M withHeader(String header, String value) {
            setHeader(header, value);
            return (M) this;
        }

        public void setHeader(String header, String value) {
            headers.put(header, value);
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        @SuppressWarnings("unchecked")
        public M withBody(String body) {
            setBody(body);
            return (M) this;
        }

        public Http.MediaType getContentType() {
            if (hasHeaders() && hasHeader(Headers.CONTENT_TYPE)) {
                return Http.MediaType.fromString(getHeader(Headers.CONTENT_TYPE));
            }
            return null;
        }

        public void setContentType(Http.MediaType contentType) {
            setHeader(Headers.CONTENT_TYPE, contentType.toString());
        }

        public M withContentType(Http.MediaType contentType) {
            return withHeader(Headers.CONTENT_TYPE, contentType.toString());
        }

    }

    private Http() { /* 🙂 */ }

}

