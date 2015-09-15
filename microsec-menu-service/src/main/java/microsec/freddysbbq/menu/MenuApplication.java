package microsec.freddysbbq.menu;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

import microsec.freddysbbq.menu.model.v1.MenuItem;

@SpringBootApplication
@EntityScan(basePackageClasses = MenuItem.class)
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
}