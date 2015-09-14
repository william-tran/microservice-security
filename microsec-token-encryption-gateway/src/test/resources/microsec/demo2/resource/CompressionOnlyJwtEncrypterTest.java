package microsec.demo2.resource;

import org.junit.Assert;
import org.junit.Test;

public class CompressionOnlyJwtEncrypterTest {
    private static final String BASE64_JWT = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIyYjM3YmQwYS1mY2NkLTQwNjEtYjA4Yy0xMWQ0YjlhYTM2NDYiLCJzdWIiOiI2NjA4N2EzOS01MTdjLTRhODYtOTlmYy05ZGZlNmZhM2FiYTkiLCJzY29wZSI6WyJvcGVuaWQiLCJ6b25lcy4xMjM0NTY3ODkuYWRtaW4iXSwiY2xpZW50X2lkIjoiaWRlbnRpdHkiLCJjaWQiOiJpZGVudGl0eSIsImF6cCI6ImlkZW50aXR5IiwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInVzZXJfaWQiOiI2NjA4N2EzOS01MTdjLTRhODYtOTlmYy05ZGZlNmZhM2FiYTkiLCJ1c2VyX25hbWUiOiJtYXJpc3NhIiwiZW1haWwiOiJtYXJpc3NhQHRlc3Qub3JnIiwiaWF0IjoxNDIzODY5MTgzLCJleHAiOjE0MjM5MTIzODMsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC91YWEvb2F1dGgvdG9rZW4iLCJ6aWQiOiJ1YWEiLCJhdWQiOlsiaWRlbnRpdHkiLCJ6b25lcy4xMjM0NTY3ODkiLCJvcGVuaWQiXX0.f8Ki8NuEHA8devX_DfQuO7EywSOQ-VBS1rcMYAXVJIQ";

    @Test
    public void testSymmetry() {
        JwtEncrypter jwtEncrypter = new CompressionOnlyJwtEncrypter();
        String encrypted = jwtEncrypter.encrypt(BASE64_JWT);
        String decrypted = jwtEncrypter.decrypt(encrypted);
        Assert.assertEquals(BASE64_JWT, decrypted);
        Assert.assertNotEquals(BASE64_JWT, encrypted);
        Assert.assertTrue(encrypted.length() < BASE64_JWT.length());
    }

}
