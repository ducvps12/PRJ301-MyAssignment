package com.acme.tags;

import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Dùng trong JSP:
 *   <mytag:ToVietnameseDate value="${requestScope.data}" />
 * Tuỳ chọn:
 *   pattern   : mẫu định dạng DateTimeFormatter (vd "dd/MM/yyyy").
 *               Nếu để "words" (mặc định) sẽ in "ngay d thang m nam y"
 *               (không dấu). Đặt diacritics="true" để có dấu.
 *   zone      : mã múi giờ IANA, mặc định "Asia/Ho_Chi_Minh".
 *   diacritics: true/false (mặc định false) -> "ngày/tháng/năm".
 *   uppercase : true/false in HOA toàn bộ (mặc định false).
 *   nullText  : chuỗi hiển thị khi value null/không parse được (mặc định rỗng).
 */
public class ToVietnameseDateTag extends SimpleTagSupport {

    private Object value;
    private String pattern = "words"; // "words" | DateTimeFormatter pattern
    private String zone = "Asia/Ho_Chi_Minh";
    private boolean diacritics = false;
    private boolean uppercase = false;
    private String nullText = "";

    // --- setters cho JSP attributes ---
    public void setValue(Object value) { this.value = value; }
    public void setPattern(String pattern) { if (pattern != null) this.pattern = pattern.trim(); }
    public void setZone(String zone) { if (zone != null && !zone.isBlank()) this.zone = zone.trim(); }
    public void setDiacritics(boolean diacritics) { this.diacritics = diacritics; }
    public void setUppercase(boolean uppercase) { this.uppercase = uppercase; }
    public void setNullText(String nullText) { this.nullText = (nullText == null ? "" : nullText); }

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();

        ZonedDateTime zdt = toZonedDateTime(value, zone);
        if (zdt == null) {
            out.write(nullText);
            return;
        }

        String result;
        if ("words".equalsIgnoreCase(pattern)) {
            int d = zdt.getDayOfMonth();
            int m = zdt.getMonthValue();
            int y = zdt.getYear();
            if (diacritics) {
                result = "ngày " + d + " tháng " + m + " năm " + y;
            } else {
                result = "ngay " + d + " thang " + m + " nam " + y;
            }
        } else {
            // format theo pattern bất kỳ
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                    pattern, Locale.forLanguageTag("vi-VN"));
            result = zdt.format(fmt);
        }

        if (uppercase) result = result.toUpperCase(Locale.ROOT);
        out.write(result);
    }

    // --- helpers ---
    private static ZonedDateTime toZonedDateTime(Object v, String zoneId) {
        if (v == null) return null;
        ZoneId zone = ZoneId.of(zoneId == null || zoneId.isBlank() ? "Asia/Ho_Chi_Minh" : zoneId);

        try {
            if (v instanceof java.util.Date) {
                return Instant.ofEpochMilli(((java.util.Date) v).getTime()).atZone(zone);
            }
            if (v instanceof java.sql.Timestamp) {
                return Instant.ofEpochMilli(((java.sql.Timestamp) v).getTime()).atZone(zone);
            }
            if (v instanceof java.sql.Date) {
                return ((java.sql.Date) v).toLocalDate().atStartOfDay(zone);
            }
            if (v instanceof Instant) {
                return ((Instant) v).atZone(zone);
            }
            if (v instanceof LocalDateTime) {
                return ((LocalDateTime) v).atZone(zone);
            }
            if (v instanceof LocalDate) {
                return ((LocalDate) v).atStartOfDay(zone);
            }
            if (v instanceof ZonedDateTime) {
                return ((ZonedDateTime) v).withZoneSameInstant(zone);
            }
            if (v instanceof OffsetDateTime) {
                return ((OffsetDateTime) v).atZoneSameInstant(zone);
            }
            if (v instanceof Number) { // epoch millis
                long epochMs = ((Number) v).longValue();
                return Instant.ofEpochMilli(epochMs).atZone(zone);
            }
            if (v instanceof CharSequence) {
                String s = v.toString().trim();
                // thử ISO_DATE_TIME trước, không được thì ISO_DATE, cuối cùng epoch millis
                try { return ZonedDateTime.parse(s).withZoneSameInstant(zone); } catch (Exception ignore) {}
                try { return OffsetDateTime.parse(s).atZoneSameInstant(zone); } catch (Exception ignore) {}
                try { return LocalDateTime.parse(s).atZone(zone); } catch (Exception ignore) {}
                try { return LocalDate.parse(s).atStartOfDay(zone); } catch (Exception ignore) {}
                try { return Instant.ofEpochMilli(Long.parseLong(s)).atZone(zone); } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
