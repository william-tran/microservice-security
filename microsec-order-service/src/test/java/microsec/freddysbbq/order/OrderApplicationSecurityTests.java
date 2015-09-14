package microsec.freddysbbq.order;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;

import microsec.freddysbbq.menu.model.v1.MenuItem;
import microsec.freddysbbq.order.model.v1.Order;
import microsec.freddysbbq.order.model.v1.OrderItem;
import microsec.test.SecurityIntegrationTest;
import microsec.test.UaaJwtToken;
import microsec.test.UaaJwtToken.UaaJwtTokenBuilder;
import microsec.uaa.model.v2.UserInfo;

@SpringApplicationConfiguration(classes = OrderApplication.class)
@TestPropertySource(properties = "security.require-ssl=true")
public class OrderApplicationSecurityTests extends SecurityIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    private OAuth2RestTemplate mockRestTemplate = mock(OAuth2RestTemplate.class);

    @Autowired
    private CustomerOrderController controller;

    @Before
    public void before() {
        reset(mockRestTemplate);
    }

    @PostConstruct
    public void init() {
        if (orderRepository.count() == 0) {
            Order order = orderFixture();
            orderRepository.save(order);
        }
        controller.setOAuth2RestTemplate(mockRestTemplate);
    }

    private Order orderFixture() {
        Order order = new Order();
        order.setCustomerId("12345");
        order.setEmail("frank@whitehouse.gov");
        order.setFirstName("Frank");
        order.setLastName("Underwood");
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setMenuItemId(1);
        orderItem.setName("full rack of ribs");
        orderItem.setPrice(new BigDecimal(20));
        orderItem.setQuantity(1);
        order.setOrderItems(Collections.singleton(orderItem));
        return order;
    }

    @Test
    public void testOrdersSecurity() throws Exception {
        checkHttpsRedirect("/orders");

        HttpResponse response = httpsRequest("/orders");
        Assert.assertEquals(401, response.getStatusLine().getStatusCode());

        UaaJwtTokenBuilder tokenBuilder = UaaJwtToken.builder();
        UaaJwtToken token = tokenBuilder.build();
        response = httpsRequest("/orders", token);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        token.setAud(Arrays.asList("order"));
        response = httpsRequest("/orders", token);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        // test for admin scope

        String body = new ObjectMapper().writeValueAsString(orderFixture());

        response = httpsRequest(HttpMethod.POST, "/orders", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.PUT, "/orders/1", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.DELETE, "/orders/1", token, null, null);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        // set the correct scope
        token.setScope(Arrays.asList("order.admin"));

        response = httpsRequest("/orders", token);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.POST, "/orders", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(201, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.PUT, "/orders/1", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(204, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.DELETE, "/orders/1", token, null, null);
        Assert.assertEquals(204, response.getStatusLine().getStatusCode());

    }

    @Test
    public void testCustomerOrdersSecurity() throws Exception {
        checkHttpsRedirect("/myorders");

        HttpResponse response = httpsRequest("/myorders");
        Assert.assertEquals(401, response.getStatusLine().getStatusCode());

        UaaJwtToken token = UaaJwtToken.builder()
                .user(UUID.randomUUID().toString(), "test", "test@test.com")
                .build();
        response = httpsRequest("/myorders", token);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        token.setAud(Arrays.asList("order"));
        response = httpsRequest("/myorders", token);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        String body = new ObjectMapper().writeValueAsString(Collections.singletonMap(1, 1));
        response = httpsRequest(HttpMethod.POST, "/myorders", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        // set the correct scope
        token.setScope(Arrays.asList("order.me"));

        response = httpsRequest("/myorders", token);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        // set up mock behaviours on OAuth2RestTemplate
        when(mockRestTemplate.getForObject(anyString(), eq(UserInfo.class))).thenReturn(new UserInfo());
        MenuItem menuItem = new MenuItem();
        menuItem.setName("test");
        menuItem.setPrice(new BigDecimal(1));
        when(mockRestTemplate.getForObject(anyString(), eq(MenuItem.class), any(), any()))
                .thenReturn(menuItem);

        response = httpsRequest(HttpMethod.POST, "/myorders", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(201, response.getStatusLine().getStatusCode());

    }

}