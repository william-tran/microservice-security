package microsec.common;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("branding")
@Data
public class Branding {
    private String restaurantName;
    private String menuTitle;
}
