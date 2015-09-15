package microsec.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Configuration
public class DumpTokenEndpointConfig {

    @Bean
    public DumpTokenController dumpTokenController() {
        return new DumpTokenController();
    }

    @Controller
    @ConditionalOnBean(DumpTokenEndpointConfig.class)
    public static class DumpTokenController {
        @Autowired
        private OAuth2ClientContext oAuth2ClientContext;

        @RequestMapping("/dump_token")
        public String dumpToken(Model model) {
            String rawToken = oAuth2ClientContext.getAccessToken().getValue();
            String decodedToken = null;
            try {
                decodedToken = Utils.toPrettyJsonString(Utils.getToken(oAuth2ClientContext));
            } catch (Exception e) {
            }
            model.addAttribute("decodedToken", decodedToken);
            model.addAttribute("rawToken", rawToken);
            return "dump_token";
        }
    }
}
