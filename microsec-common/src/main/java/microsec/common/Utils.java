package microsec.common;

import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.springframework.security.oauth2.client.OAuth2ClientContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toPrettyJsonString(Object object) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    public static Map<String, ?> getToken(OAuth2ClientContext clientContext) throws Exception {
        if (clientContext.getAccessToken() != null) {
            String tokenBase64 = clientContext.getAccessToken().getValue().split("\\.")[1];
            return objectMapper.readValue(Base64.decodeBase64(tokenBase64), new TypeReference<Map<String, ?>>() {
            });
        }
        return null;
    }
}
