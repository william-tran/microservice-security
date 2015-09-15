package microsec.freddysbbq.customer_app;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.cloud.security.oauth2.sso.EnableOAuth2Sso;
import org.springframework.cloud.security.oauth2.sso.OAuth2SsoConfigurerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import microsec.common.DumpTokenEndpointConfig;
import microsec.freddysbbq.menu.model.v1.MenuItem;

@SpringBootApplication
@Controller
@EnableOAuth2Sso
@Import(DumpTokenEndpointConfig.class)
public class CustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }

    @Bean
    public OAuth2SsoConfigurerAdapter oAuth2SsoConfigurerAdapter(SecurityProperties securityProperties) {
        return new OAuth2SsoConfigurerAdapter() {
            @Override
            public void match(RequestMatchers matchers) {
                matchers.antMatchers("/**");
            }

            @Override
            public void configure(HttpSecurity http) throws Exception {
                if (securityProperties.isRequireSsl()) {
                    http.requiresChannel().anyRequest().requiresSecure();
                }
                http.authorizeRequests().anyRequest().authenticated();
            }
        };
    }

    @RequestMapping("/")
    public String index(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
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