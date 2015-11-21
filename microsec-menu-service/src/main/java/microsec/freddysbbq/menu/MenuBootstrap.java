package microsec.freddysbbq.menu;

import java.util.List;

import lombok.Data;
import microsec.freddysbbq.menu.model.v1.MenuItem;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("menuBootstrap")
@Data
public class MenuBootstrap {
    private List<MenuItem> items;
}
