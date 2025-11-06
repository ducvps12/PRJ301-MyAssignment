package com.acme.leavemgmt.util;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing/verification using PBKDF2WithHmacSHA256.
 * Format lưu trữ:  PBKDF2$<iter>$<saltB64>$<hashB64>
 */
public final class Passwords {
    public  static final String SCHEME   = "PBKDF2";
    private static final String ALGO     = "PBKDF2WithHmacSHA256";
    private static final int    ITER     = 120_000;  // tăng dần khi phần cứng cho phép
    private static final int    SALT_LEN = 16;       // bytes
    private static final int    KEY_LEN  = 256;      // bits
    private static final SecureRandom RNG = new SecureRandom();

    private Passwords(){}

    /* ===================== Public API ===================== */

    /** Hash từ String (tiện lợi). */
    public static String hash(String plain) {
        if (plain == null) throw new IllegalArgumentException("plain == null");
        return hash(plain.toCharArray(), ITER, SALT_LEN, KEY_LEN);
    }

    /** Hash với cấu hình tùy biến (nếu cần nâng – test/benchmark). */
    public static String hash(String plain, int iterations, int saltLenBytes, int keyLenBits) {
        if (plain == null) throw new IllegalArgumentException("plain == null");
        return hash(plain.toCharArray(), iterations, saltLenBytes, keyLenBits);
    }

    /** Kiểm tra mật khẩu nhập với chuỗi đã lưu. */
    public static boolean verify(String plain, String stored) {
        if (plain == null || stored == null) return false;
        try {
            Parsed p = parse(stored);
            byte[] test = pbkdf2(plain.toCharArray(), p.salt, p.iter, p.hash.length * 8);
            return slowEquals(p.hash, test);
        } catch (Exception ignore) {
            return false;
        }
    }

    /**
     * Gợi ý re-hash: true nếu hash cũ không theo scheme/iter/keylen hiện tại.
     * Gọi sau khi verify(true) để nâng cấp hash khi người dùng đăng nhập.
     */
    public static boolean needsRehash(String stored) {
        try {
            Parsed p = parse(stored);
            return (!SCHEME.equals(p.scheme)) || p.iter < ITER || (p.hash.length * 8) < KEY_LEN;
        } catch (Exception e) {
            // định dạng lạ -> nên rehash
            return true;
        }
    }

    /* ===================== Internal ===================== */

    private static String hash(char[] password, int iterations, int saltLenBytes, int keyLenBits) {
        if (iterations < 20_000) throw new IllegalArgumentException("iterations quá thấp");
        if (saltLenBytes < 8)    throw new IllegalArgumentException("salt quá ngắn");
        if (keyLenBits < 128)    throw new IllegalArgumentException("keyLen quá thấp");

        byte[] salt = new byte[saltLenBytes];
        RNG.nextBytes(salt);

        byte[] dk = pbkdf2(password, salt, iterations, keyLenBits);
        zero(password); // dọn dẹp

        return SCHEME + "$" + iterations + "$" + b64(salt) + "$" + b64(dk);
    }

    private static class Parsed {
        final String scheme; final int iter; final byte[] salt; final byte[] hash;
        Parsed(String scheme, int iter, byte[] salt, byte[] hash) {
            this.scheme = scheme; this.iter = iter; this.salt = salt; this.hash = hash;
        }
    }

    private static Parsed parse(String stored) {
        String[] p = stored.split("\\$");
        if (p.length != 4) throw new IllegalArgumentException("Bad format");
        String scheme = p[0];
        int iter = Integer.parseInt(p[1]);
        byte[] salt = Base64.getDecoder().decode(p[2]);
        byte[] hash = Base64.getDecoder().decode(p[3]);
        return new Parsed(scheme, iter, salt, hash);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iter, int bits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iter, bits);
            byte[] out = SecretKeyFactory.getInstance(ALGO).generateSecret(spec).getEncoded();
            spec.clearPassword(); // dọn dẹp
            return out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String b64(byte[] b){ return Base64.getEncoder().encodeToString(b); }

    private static boolean slowEquals(byte[] a, byte[] b) {
        if (a == null || b == null) return false;
        int diff = a.length ^ b.length;
        for (int i = 0; i < Math.min(a.length, b.length); i++) diff |= a[i] ^ b[i];
        return diff == 0;
    }

    private static void zero(char[] a){
        if (a == null) return;
        for (int i = 0; i < a.length; i++) a[i] = 0;
    }
}
