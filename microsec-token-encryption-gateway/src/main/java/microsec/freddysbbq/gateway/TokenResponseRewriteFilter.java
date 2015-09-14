package microsec.freddysbbq.gateway;

import java.util.Map;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class TokenResponseRewriteFilter extends ZuulFilter {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtEncrypter jwtEncrypter;

    public TokenResponseRewriteFilter(JwtEncrypter jwtEncrypter) {
        super();
        this.jwtEncrypter = jwtEncrypter;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext context = RequestContext.getCurrentContext();
        return context.getRequest().getRequestURI().endsWith("/oauth/token") ||
                context.getRequest().getRequestURI().endsWith("/oauth/token/")
                && context.getRequest().getMethod().toLowerCase().equals("post")
                && isOk(context) && containsContent(context) && isJson(context);
    }

    @Override
    public Object run() {
        try {
            final RequestContext context = RequestContext.getCurrentContext();
            Map<String, Object> response = null;
            if (context.getResponseBody() != null) {
                response = objectMapper.readValue(context.getResponseBody(), new TypeReference<Map<String, Object>>() {
                });
            } else if (context.getResponseDataStream() != null) {
                response = objectMapper.readValue(context.getResponseDataStream(),
                        new TypeReference<Map<String, Object>>() {
                        });
            }
            if (response != null) {
                rewriteTokenResponse(response);
                context.setResponseBody(objectMapper.writeValueAsString(response));
            }

        } catch (final Exception e) {
            Throwables.propagate(e);
        }
        return null;
    }

    protected void rewriteTokenResponse(Map<String, Object> response) {
        rewriteJwtValue("access_token", response);
        rewriteJwtValue("id_token", response);
        rewriteJwtValue("refresh_token", response);
    }

    protected void rewriteJwtValue(String key, Map<String, Object> response) {
        Object value = response.get(key);
        if (value instanceof String) {
            response.put(key, jwtEncrypter.encrypt(response.get(key).toString()));
        }
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public String filterType() {
        return "post";
    }

    private static boolean isOk(final RequestContext context) {
        assert context != null;
        return context.getResponseStatusCode() == 200;
    }

    private static boolean containsContent(final RequestContext context) {
        assert context != null;
        return context.getResponseDataStream() != null || context.getResponseBody() != null;
    }

    private static boolean isJson(final RequestContext context) {
        return getResponseMediaType(context).includes(MediaType.APPLICATION_JSON);
    }

    private static MediaType getResponseMediaType(final RequestContext context) {
        assert context != null;
        for (final Pair<String, String> header : context.getZuulResponseHeaders()) {
            if (header.first().equalsIgnoreCase(CONTENT_TYPE)) {
                return MediaType.parseMediaType(header.second());
            }
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

}
