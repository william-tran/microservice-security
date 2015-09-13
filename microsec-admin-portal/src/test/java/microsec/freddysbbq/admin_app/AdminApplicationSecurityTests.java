package microsec.freddysbbq.admin_app;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.security.oauth2.sso.OAuth2SsoProperties;
import org.springframework.test.context.TestPropertySource;

import microsec.test.SecurityIntegrationTest;

@SpringApplicationConfiguration(classes = AdminApplication.class)
@TestPropertySource(properties = "security.require-ssl=true")
public class AdminApplicationSecurityTests extends SecurityIntegrationTest {

    @Autowired
    private OAuth2SsoProperties ssoProperties;

    @Test
    public void testHomepageSecurity() throws Exception {
        checkRequiresHttpsAndOauthSso("/", ssoProperties.getLoginPath());
    }

    @Test
    public void testMenuSecurity() throws Exception {
        checkRequiresHttpsAndOauthSso("/menuItems", ssoProperties.getLoginPath());
        checkRequiresHttpsAndOauthSso("/menuItems/new", ssoProperties.getLoginPath());
        checkRequiresHttpsAndOauthSso("/menuItems/1", ssoProperties.getLoginPath());
        checkRequiresHttpsAndOauthSso("/menuItems/1/delete", ssoProperties.getLoginPath());
    }

    @Test
    public void testOrderSecurity() throws Exception {
        checkRequiresHttpsAndOauthSso("/orders/", ssoProperties.getLoginPath());
        checkRequiresHttpsAndOauthSso("/orders/1/delete", ssoProperties.getLoginPath());
    }

}