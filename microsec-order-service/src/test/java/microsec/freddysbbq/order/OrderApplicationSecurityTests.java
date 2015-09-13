package microsec.freddysbbq.order;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;

import microsec.freddysbbq.order.model.v1.Order;
import microsec.freddysbbq.order.model.v1.OrderItem;
import microsec.test.SecurityIntegrationTest;
import microsec.test.UaaJwtToken;
import microsec.test.UaaJwtToken.UaaJwtTokenBuilder;

@SpringApplicationConfiguration(classes = OrderApplication.class)
@TestPropertySource(properties = "security.require-ssl=true")
public class OrderApplicationSecurityTests extends SecurityIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @PostConstruct
    public void bootstrapOrder() {
        if (orderRepository.count() == 0) {
            Order order = orderFixture();
            orderRepository.save(order);
        }
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

}