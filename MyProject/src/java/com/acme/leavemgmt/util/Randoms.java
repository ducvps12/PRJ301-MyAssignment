// com.acme.leavemgmt.util.Randoms
package com.acme.leavemgmt.util;


import java.security.SecureRandom;


public final class Randoms {
private static final SecureRandom R = new SecureRandom();


public static String otp6() {
int n = R.nextInt(1_000_000); // 0..999999
return String.format("%06d", n);
}


public static String tempPassword(int len) {
final String a = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$%"; // no confusing 0/O/1/l
StringBuilder sb = new StringBuilder(len);
for (int i = 0; i < len; i++) sb.append(a.charAt(R.nextInt(a.length())));
return sb.toString();
}
}