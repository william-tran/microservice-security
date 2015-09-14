package microsec.freddysbbq.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableZuulProxy
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public JwtEncrypter jwtEncrypter() {
        return new CompressionOnlyJwtEncrypter();
    }

    @Bean
    public TokenResponseRewriteFilter tokenResponseRewriteFilter(JwtEncrypter jwtEncrypter) {
        return new TokenResponseRewriteFilter(jwtEncrypter);
    }

    @Bean
    public TokenHeaderDecryptAndVerify tokenHeaderDecrypter(JwtEncrypter jwtEncrypter) {
        return new TokenHeaderDecryptAndVerify(jwtEncrypter);
    }

}