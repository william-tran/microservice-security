package microsec.freddysbbq.customer_app;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.security.oauth2.sso.OAuth2SsoProperties;
import org.springframework.test.context.TestPropertySource;

import microsec.test.SecurityIntegrationTest;

@SpringApplicationConfiguration(classes = CustomerApplication.class)
@TestPropertySource(properties = "security.require-ssl=true")
public class CustomerApplicationSecurityTests extends SecurityIntegrationTest {

    @Autowired
    private OAuth2SsoProperties ssoProperties;

    @Test
    public void testHomepageSecurity() throws Exception {
        checkRequiresHttpsAndOauthSso("/", ssoProperties.getLoginPath());
    }

    @Test
    public void testMenuSecurity() throws Exception {
        checkRequiresHttpsAndOauthSso("/menu", ssoProperties.getLoginPath());
    }

    @Test
    public void testMyOrdersSecurity() throws Exception {
        checkRequiresHttpsAndOauthSso("/myorders", ssoProperties.getLoginPath());
    }

}