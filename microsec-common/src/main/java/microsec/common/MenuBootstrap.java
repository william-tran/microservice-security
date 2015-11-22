package microsec.common;

import java.util.List;

import lombok.Data;
import microsec.freddysbbq.menu.model.v1.MenuItem;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("menuBootstrap")
@Data
public class MenuBootstrap {
    private List<MenuItem> items;
}
