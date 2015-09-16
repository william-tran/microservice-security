package microsec.freddysbbq.menu;

import java.math.BigDecimal;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;

import microsec.freddysbbq.menu.model.v1.MenuItem;
import microsec.test.SecurityIntegrationTest;
import microsec.test.UaaJwtToken;
import microsec.test.UaaJwtToken.UaaJwtTokenBuilder;

@SpringApplicationConfiguration(classes = MenuApplication.class)
@TestPropertySource(properties = "security.require-ssl=true")
public class MenuApplicationSecurityTests extends SecurityIntegrationTest {

    @Test
    public void testMenuItemsSecurity() throws Exception {
        checkHttpsRedirect("/menuItems");

        HttpResponse response = httpsRequest("/menuItems");
        Assert.assertEquals(401, response.getStatusLine().getStatusCode());

        UaaJwtTokenBuilder tokenBuilder = UaaJwtToken.builder();
        UaaJwtToken token = tokenBuilder.build();
        response = httpsRequest("/menuItems", token);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        token.setAud(Arrays.asList("menu"));
        token.setScope(Arrays.asList("foo.bar"));
        response = httpsRequest("/menuItems", token);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        token.setScope(Arrays.asList("menu.read"));
        response = httpsRequest("/menuItems", token);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        // test for write scope

        MenuItem menuItem = new MenuItem();
        menuItem.setName("test");
        menuItem.setPrice(new BigDecimal(1));
        String body = new ObjectMapper().writeValueAsString(new MenuItem());

        response = httpsRequest(HttpMethod.POST, "/menuItems", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.PUT, "/menuItems/1", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.DELETE, "/menuItems/1", token, null, null);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        // set the correct scope
        token.setScope(Arrays.asList("menu.write"));

        response = httpsRequest(HttpMethod.POST, "/menuItems", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(201, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.PUT, "/menuItems/1", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(204, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.DELETE, "/menuItems/1", token, null, null);
        Assert.assertEquals(204, response.getStatusLine().getStatusCode());

    }

}