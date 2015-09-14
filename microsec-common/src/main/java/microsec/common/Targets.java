package microsec.common;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Uris to other components
 * 
 * @author will.tran
 *
 */
@Data
@ConfigurationProperties("targets")
public class Targets {
	private String scheme;
    private String uaa;
    private String order;
    private String menu;
}
