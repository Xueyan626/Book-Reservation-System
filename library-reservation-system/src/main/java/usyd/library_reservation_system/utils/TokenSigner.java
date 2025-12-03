package usyd.library_reservation_system.library_reservation_system.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public class TokenSigner {

    public static String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String buildChallenge(String email, String code, long expireEpochSeconds, String secret) {
        // payload contains: email|code|exp
        String payload = email.toLowerCase() + "|" + code + "|" + expireEpochSeconds;
        String sig = sign(payload, secret);
        String token = payload + "|" + sig;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    public static Decoded decodeAndVerify(String token, String secret) {
        String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        String[] parts = raw.split("\\|");
        if (parts.length != 4) throw new IllegalArgumentException("bad token");
        String email = parts[0];
        String code  = parts[1];
        long exp     = Long.parseLong(parts[2]);
        String sig   = parts[3];

        String expect = sign(email.toLowerCase() + "|" + code + "|" + exp, secret);
        if (!expect.equals(sig)) throw new IllegalArgumentException("bad signature");
        if (Instant.now().getEpochSecond() > exp) throw new IllegalArgumentException("expired");
        return new Decoded(email, code, exp);
    }

    public record Decoded(String email, String code, long exp) {}
}
