package microsec.freddysbbq.menu;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.cloud.security.oauth2.resource.EnableOAuth2Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

import microsec.freddysbbq.menu.model.v1.MenuItem;

@SpringBootApplication
@EntityScan(basePackageClasses = MenuItem.class)
@EnableOAuth2Resource
public class MenuApplication {

    public static void main(String[] args) {
        SpringApplication.run(MenuApplication.class, args);
    }

    @Autowired
    private MenuItemRepository menuRepository;

    @PostConstruct
    public void bootstrap() {
        if (menuRepository.count() == 0) {
            MenuItem menuItem = new MenuItem();
            menuItem.setName("full rack of ribs");
            menuItem.setPrice(new BigDecimal(20));
            menuRepository.save(menuItem);
        }
    }

    @Configuration
    public static class RepositoryConfig extends RepositoryRestMvcConfiguration {
        @Override
        protected void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
            config.exposeIdsFor(MenuItem.class);
        }
    }

    @Bean
    public ResourceServerConfigurer resourceServer(SecurityProperties securityProperties) {
        return new ResourceServerConfigurerAdapter() {
            @Override
            public void configure(ResourceServerSecurityConfigurer resources) {
                resources.resourceId("menu");
            }

            @Override
            public void configure(HttpSecurity http) throws Exception {
                if (securityProperties.isRequireSsl()) {
                    http.requiresChannel().anyRequest().requiresSecure();
                }
                http
                        .authorizeRequests()
                        .antMatchers(HttpMethod.GET, "/**").access("#oauth2.hasScope('menu.read')")
                        .antMatchers(HttpMethod.POST, "/**").access("#oauth2.hasScope('menu.write')")
                        .antMatchers(HttpMethod.PUT, "/**").access("#oauth2.hasScope('menu.write')")
                        .antMatchers(HttpMethod.DELETE, "/**").access("#oauth2.hasScope('menu.write')");
            }
        };
    }
}