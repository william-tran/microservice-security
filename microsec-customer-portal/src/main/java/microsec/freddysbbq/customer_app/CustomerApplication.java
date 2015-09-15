package microsec.freddysbbq.customer_app;

import java.math.BigDecimal;
import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import microsec.freddysbbq.menu.model.v1.MenuItem;

@SpringBootApplication
@Controller
public class CustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("username", "Will");
        return "index";
    }

    @RequestMapping("/menu")
    public String menu(Model model) throws Exception {
        MenuItem menuItem = new MenuItem();
        menuItem.setName("ribs");
        menuItem.setPrice(new BigDecimal(10.00));
        model.addAttribute("menu", Collections.singleton(menuItem));
        return "menu";
    }

}