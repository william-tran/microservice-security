package microsec.uaa.model.v2;

import lombok.Data;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Data
@JsonNaming(LowerCaseWithUnderscoresStrategy.class)
public class UserInfo {
    private String userId;
    private String userName;
    private String givenName;
    private String familyName;
    private String name;
    private String email;

}
