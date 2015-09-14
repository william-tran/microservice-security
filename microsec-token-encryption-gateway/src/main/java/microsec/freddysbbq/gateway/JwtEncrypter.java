package microsec.freddysbbq.gateway;

/**
 * methods expect Base64 strings and produce Base64 strings
 * 
 * @author will.tran
 *
 */
public interface JwtEncrypter {

    String encrypt(String jwt);

    String decrypt(String encryptedJwt);

}