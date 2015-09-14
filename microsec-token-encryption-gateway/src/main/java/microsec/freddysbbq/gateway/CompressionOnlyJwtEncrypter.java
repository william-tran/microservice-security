package microsec.freddysbbq.gateway;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;

/**
 * For demo purposes only, this doesn't do actual encrypt/decrypt, just
 * compress/decompress
 * 
 * @author will.tran
 *
 */
public class CompressionOnlyJwtEncrypter implements JwtEncrypter {

    @Override
    public String encrypt(String jwt) {
        try {
            byte[] compressed = compress(jwt.getBytes(StandardCharsets.US_ASCII));
            return Base64.encodeBase64String(compressed);
        } catch (Exception e) {
            throw new JwtEncrypterException("encrypt failed", e);
        }
    }

    @Override
    public String decrypt(String encryptedJwt) {
        try {
            byte[] compressed = Base64.decodeBase64(encryptedJwt);
            byte[] decompressed = decompress(compressed);
            return new String(decompressed, StandardCharsets.US_ASCII);
        } catch (Exception e) {
            throw new JwtEncrypterException("encrypt failed", e);
        }
    }

    private static byte[] decompress(byte[] bytes) throws IOException, DataFormatException {

        Inflater decompresser = new Inflater();
        decompresser.setInput(bytes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);

        byte[] buf = new byte[1024];
        while (!decompresser.finished()) {
            int count = decompresser.inflate(buf);
            bos.write(buf, 0, count);
        }
        decompresser.end();
        bos.close();
        return bos.toByteArray();
    }

    private static byte[] compress(byte[] bytes) throws IOException {
        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);
        compressor.setInput(bytes);
        compressor.finish();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);

        byte[] buf = new byte[1024];
        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }
        bos.close();
        return bos.toByteArray();
    }

    public static class JwtEncrypterException extends RuntimeException {

        public JwtEncrypterException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

    }

}
