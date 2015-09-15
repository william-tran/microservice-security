package microsec.test;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpMethod;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest()
@TestPropertySource(properties = {
        "server.port=0",
        "spring.oauth2.resource.jwt.keyValue=test"
})
public abstract class SecurityIntegrationTest {

    protected HttpClient httpClient;

    @Value("${local.server.port}")
    protected int port;

    @Before
    public void setup() {
        httpClient = HttpClientBuilder.create()
                .disableRedirectHandling()
                .disableCookieManagement()
                .build();
    }

    protected void checkHttpsRedirect(String uri) throws IOException, ClientProtocolException {
        HttpResponse response = httpRequest(uri);
        Assert.assertEquals(302, response.getStatusLine().getStatusCode());
        Assert.assertEquals("https://localhost" + uri, response.getFirstHeader("Location").getValue());
    }

    protected HttpResponse httpRequest(String uri) throws ClientProtocolException, IOException {
        HttpGet get = new HttpGet(UriComponentsBuilder.fromUriString(uri)
                .scheme("http")
                .host("localhost")
                .port(port)
                .build().toUri());
        get.setHeader("x-forwarded-port", "80");
        get.setHeader("x-forwarded-proto", "http");
        HttpResponse response = httpClient.execute(get);
        get.abort();
        EntityUtils.consumeQuietly(response.getEntity());
        return response;
    }

    protected HttpResponse httpsRequest(String uri) throws ClientProtocolException, IOException {
        return httpsRequest(uri, (Map<String, String>) null);
    }

    protected HttpResponse httpsRequest(String uri, Map<String, String> headers) throws ClientProtocolException,
            IOException {
        return httpsRequest(HttpMethod.GET, uri, headers, null, null);
    }

    protected HttpResponse httpsRequest(HttpMethod method, String uriString, Map<String, String> headers,
            ContentType contentType, String body) throws ClientProtocolException,
                    IOException {
        HttpUriRequest request = null;
        URI uri = UriComponentsBuilder.fromUriString(uriString)
                .scheme("http")
                .host("localhost")
                .port(port)
                .build().toUri();
        HttpEntity entity = null;
        if (body != null) {
            entity = new StringEntity(body, contentType);
        }
        if (method == HttpMethod.GET) {
            request = new HttpGet(uri);
        } else if (method == HttpMethod.DELETE) {
            request = new HttpDelete(uri);
        } else if (method == HttpMethod.POST) {
            HttpPost post = new HttpPost(uri);
            post.setEntity(entity);
            request = post;
        } else if (method == HttpMethod.PUT) {
            HttpPut put = new HttpPut(uri);
            put.setEntity(entity);
            request = put;
        }
        request.setHeader("x-forwarded-port", "443");
        request.setHeader("x-forwarded-proto", "https");
        if (headers != null) {
            HttpMessage message = request;
            headers.forEach((k, v) -> message.setHeader(k, v));
        }
        HttpResponse response = httpClient.execute(request);
        request.abort();
        EntityUtils.consumeQuietly(response.getEntity());
        return response;
    }

    protected void checkRequiresHttpsAndOauthSso(String uri, String loginPath)
            throws IOException, ClientProtocolException {
        checkHttpsRedirect(uri);
        HttpResponse response = httpsRequest(uri);
        Assert.assertEquals(302, response.getStatusLine().getStatusCode());
        String expectedLocation = UriComponentsBuilder.fromUriString(loginPath).host("localhost").scheme("https")
                .toUriString();
        Assert.assertEquals(expectedLocation, response.getFirstHeader("Location").getValue());
    }

    private MacSigner signer = new MacSigner("test");
    private ObjectMapper objectMapper = new ObjectMapper();

    protected HttpResponse httpsRequest(String uri, UaaJwtToken token)
            throws ClientProtocolException, IOException {
        return httpsRequest(HttpMethod.GET, uri, token, null, null);
    }

    protected HttpResponse httpsRequest(HttpMethod method, String uri, UaaJwtToken token, ContentType contentType,
            String body)
                    throws ClientProtocolException, IOException {
        String tokenValue = JwtHelper.encode(objectMapper.writeValueAsString(token), signer).getEncoded();

        return httpsRequest(method, uri, Collections.singletonMap("Authorization", "Bearer " + tokenValue), contentType,
                body);
    }

}