package microsec.sso;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.cloud.security.oauth2.resource.ResourceServerProperties;
import org.springframework.cloud.security.oauth2.sso.EnableOAuth2Sso;
import org.springframework.cloud.security.oauth2.sso.OAuth2SsoConfigurerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@Controller
@EnableOAuth2Sso
public class OpenIdSsoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenIdSsoApplication.class, args);
    }

    @Bean
    public OAuth2SsoConfigurerAdapter oAuth2SsoConfigurerAdapter(SecurityProperties securityProperties) {
        return new OAuth2SsoConfigurerAdapter() {
            @Override
            public void match(RequestMatchers matchers) {
                matchers.antMatchers("/userinfo");
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

    @Autowired
    private ResourceServerProperties resourceServerProperties;

    @Autowired
    private OAuth2RestTemplate oauth2RestTemplate;

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping("/userinfo")
    public String userinfo(Model model, Principal principal) throws Exception {
        Map<?, ?> userInfoResponse = oauth2RestTemplate.getForObject(resourceServerProperties.getUserInfoUri(),
                Map.class);
        model.addAttribute("username", principal.getName());
        model.addAttribute("response",
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userInfoResponse));
        model.addAttribute("token", oauth2RestTemplate.getOAuth2ClientContext().getAccessToken().getValue());
        return "userinfo";
    }

}