package microsec.freddysbbq.gateway;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class TokenHeaderDecryptAndVerify extends ZuulFilter {

    private final JwtEncrypter jwtEncrypter;

    public TokenHeaderDecryptAndVerify(JwtEncrypter jwtEncrypter) {
        super();
        this.jwtEncrypter = jwtEncrypter;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext context = RequestContext.getCurrentContext();
        String authHeader = context.getRequest().getHeader("Authorization");
        return authHeader != null
                && authHeader.length() > 7
                && authHeader.substring(0, 7).toLowerCase().equals("bearer ");
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        String authHeader = context.getRequest().getHeader("Authorization");
        String encryptedJwt = authHeader.substring(7).trim();
        String decrypted = jwtEncrypter.decrypt(encryptedJwt);
        context.addZuulRequestHeader("authorization", "bearer " + decrypted);
        return null;
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

}
