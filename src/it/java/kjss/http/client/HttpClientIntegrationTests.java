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

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import kjss.http.Http;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@WireMockTest
class HttpClientIntegrationTests {

    @Test void should_make_HTTP_GET_request(WireMockRuntimeInfo wireMockRuntimeInfo) throws Exception {
        // Given
        givenThat(
            get(urlPathEqualTo("/context/test"))
                .withQueryParam("name", equalTo("World"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(ok("Hello, World!")
                                .withHeader("Content-Type", "application/json"))
        );
        Http.Client httpClient = new HttpClient(url(wireMockRuntimeInfo.getHttpBaseUrl() + "/context"))
            .withConnectTimeout(Duration.ofSeconds(1))
            .withReadTimeout(Duration.ofMillis(500));

        // When
        Http.Request request = Http.get("/test")
                                   .withHeader("Accept", "application/json")
                                   .withParam("name", "World");
        Http.Response response = httpClient.exchange(request);

        // Then
        assertThat(response)
            .hasFieldOrPropertyWithValue("status.code", 200)
            .hasFieldOrPropertyWithValue("status.message", "OK")
            .hasFieldOrPropertyWithValue("body", "Hello, World!")
            .extracting(r -> r.getHeader("Content-Type")).isEqualTo("application/json");

    }

    @Test void should_make_HTTP_POST_request_with_Form(WireMockRuntimeInfo wireMockRuntimeInfo) throws Exception {
        // Given
        givenThat(
            post(urlPathEqualTo("/context/test"))
                .withQueryParam("name", equalTo("World"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .willReturn(
                    aResponse()
                        .withStatus(Http.Response.Status.ACCEPTED.code())
                        .withStatusMessage(Http.Response.Status.ACCEPTED.message())
                        .withBody("Hello, World!")
                        .withHeader(Http.Response.Headers.CONTENT_TYPE, Http.MediaType.APPLICATION_JSON.toString()))
        );
        Http.Client httpClient = new HttpClient(url(wireMockRuntimeInfo.getHttpBaseUrl() + "/context"))
            .withConnectTimeout(Duration.ofSeconds(1))
            .withReadTimeout(Duration.ofMillis(500));

        // When
        Http.Request request = Http.post("/test")
                                   .accept(Http.MediaType.APPLICATION_JSON)
                                   .withParam("name", "World");
        Http.Response response = httpClient.exchange(request);

        // Then
        assertThat(response)
            .hasFieldOrPropertyWithValue("status", Http.Response.Status.ACCEPTED)
            .hasFieldOrPropertyWithValue("body", "Hello, World!")
            .extracting(Http.Response::getContentType).isEqualTo(Http.MediaType.APPLICATION_JSON);

    }

    @Test void should_make_HTTP_POST_request_with_JSON_document(WireMockRuntimeInfo wireMockRuntimeInfo) throws Exception {
        // Given
        givenThat(
            post(urlPathEqualTo("/context/test"))
                .withRequestBody(equalTo("{\"name\":\"World\"}"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(Http.Response.Status.ACCEPTED.code())
                        .withStatusMessage(Http.Response.Status.ACCEPTED.message())
                        .withBody("Hello, World!")
                        .withHeader(Http.Response.Headers.CONTENT_TYPE, Http.MediaType.APPLICATION_JSON.toString()))
        );
        Http.Client httpClient = new HttpClient(url(wireMockRuntimeInfo.getHttpBaseUrl() + "/context"))
            .withConnectTimeout(Duration.ofSeconds(1))
            .withReadTimeout(Duration.ofMillis(500));

        // When
        Http.Request request = Http.post("/test")
                                   .accept(Http.MediaType.APPLICATION_JSON)
                                   .withContentType(Http.MediaType.APPLICATION_JSON)
                                   .withBody("{\"name\":\"World\"}");
        Http.Response response = httpClient.exchange(request);

        // Then
        assertThat(response)
            .hasFieldOrPropertyWithValue("status", Http.Response.Status.ACCEPTED)
            .hasFieldOrPropertyWithValue("body", "Hello, World!")
            .extracting(Http.Response::getContentType).isEqualTo(Http.MediaType.APPLICATION_JSON);

    }

    @Test void should_handle_404_Not_Found_error(WireMockRuntimeInfo wireMockRuntimeInfo) throws Exception {
        // Given
        givenThat(
            get(urlPathEqualTo("/context/unknown/path"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withStatusMessage("Not Found")
                        .withHeader("Content-Type", "text/html")
                        .withBody("<html><body>This are not the droids you're looking for!</body></html>")
                )
        );
        Http.Client httpClient = new HttpClient(url(wireMockRuntimeInfo.getHttpBaseUrl() + "/context"))
            .withConnectTimeout(Duration.ofSeconds(1))
            .withReadTimeout(Duration.ofMillis(500));

        // When
        Http.Request request = Http.get("/unknown/path");
        Http.Response response = httpClient.exchange(request);

        // Then
        assertThat(response.getStatus().is2XXSuccess())
            .isFalse();
        Http.Response.Status notFound = Http.Response.Status.NOT_FOUND;
        assertThat(response.getStatus())
            .isEqualTo(notFound);
        assertThat(response.getContentType())
            .isEqualTo(Http.MediaType.TEXT_HTML);
        assertThat(response.getBody())
            .isEqualTo("<html><body>This are not the droids you're looking for!</body></html>");
    }

    @Test void should_handle_connection_error() throws Exception {
        // Given
        Http.Client httpClient = new HttpClient(url( "http://localhost:999/context"))
            .withConnectTimeout(Duration.ofSeconds(1))
            .withReadTimeout(Duration.ofMillis(500));

        // When
        Http.Request request = Http.get("/");
        assertThatThrownBy(() -> httpClient.exchange(request))
            .isInstanceOf(IOException.class);
    }

    private static URL url(String url) {
        try {
            return URI.create(url).toURL();
        }
        catch (MalformedURLException error) {
            throw new RuntimeException(error);
        }
    }

}
