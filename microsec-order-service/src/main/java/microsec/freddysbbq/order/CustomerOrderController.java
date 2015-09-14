package microsec.freddysbbq.order;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.security.oauth2.resource.ResourceServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import lombok.Setter;
import microsec.common.Targets;
import microsec.freddysbbq.menu.model.v1.MenuItem;
import microsec.freddysbbq.order.model.v1.Order;
import microsec.freddysbbq.order.model.v1.OrderItem;
import microsec.uaa.model.v2.UserInfo;

/**
 * An Order controller for customer use
 * 
 * @author will.tran
 *
 */
@RestController
public class CustomerOrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    @Setter
    private OAuth2RestTemplate oAuth2RestTemplate;

    @Autowired
    private ResourceServerProperties resourceServerProperties;

    @Autowired
    private Targets targets;

    @PreAuthorize("#oauth2.hasScope('order.me')")
    @RequestMapping("/myorders")
    public Collection<Order> getMyOrders(Principal principal) {
        return orderRepository.findByCustomerId(principal.getName());
    }

    @PreAuthorize("#oauth2.hasScope('order.me')")
    @RequestMapping(value = "/myorders", method = RequestMethod.POST)
    public ResponseEntity<Void> placeOrder(Principal principal, @RequestBody Map<Long, Integer> itemsAndQuantites) {
        if (itemsAndQuantites.isEmpty()) {
            return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
        }
        UserInfo userInfo = oAuth2RestTemplate.getForObject(resourceServerProperties.getUserInfoUri(), UserInfo.class);
        Order order = new Order();
        order.setCustomerId(principal.getName());
        order.setEmail(userInfo.getEmail());
        order.setFirstName(userInfo.getGivenName());
        order.setLastName(userInfo.getFamilyName());
        order.setPlaced(new Date());
        Set<OrderItem> items = new LinkedHashSet<>();
        BigDecimal total = new BigDecimal(0);
        for (Entry<Long, Integer> itemAndQuantity : itemsAndQuantites.entrySet()) {
            Long itemId = itemAndQuantity.getKey();
            Integer quantity = itemAndQuantity.getValue();
            if (itemId == null || quantity != null && quantity < 0) {
                return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
            } else if (quantity == null || quantity == 0) {
                continue;
            }
            try {
                MenuItem item = oAuth2RestTemplate
                        .getForObject("{menu}/menuItems/{id}", MenuItem.class, targets.getMenu(), itemId);
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setMenuItemId(itemId);
                orderItem.setName(item.getName());
                orderItem.setPrice(item.getPrice());
                orderItem.setQuantity(quantity);
                items.add(orderItem);
                total = total.add(item.getPrice().multiply(new BigDecimal(quantity)));
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
                }
            }
        }
        order.setTotal(total);
        order.setOrderItems(items);
        orderRepository.save(order);
        return new ResponseEntity<Void>(HttpStatus.CREATED);
    }
}