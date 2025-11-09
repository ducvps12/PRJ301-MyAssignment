package com.acme.tags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/** <mytag:ToVietnameseDate value="..."/> */
public class ToVietnameseDateTag extends SimpleTagSupport {
    private Object value; // Date, LocalDate, LocalDateTime, String (ISO/dd/MM/yyyy)

    public void setValue(Object value) { this.value = value; }

    @Override
    public void doTag() throws JspException, IOException {
        if (value == null) return;

        LocalDate d;
        if (value instanceof Date) {
            d = Instant.ofEpochMilli(((Date) value).getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (value instanceof LocalDate) {
            d = (LocalDate) value;
        } else if (value instanceof LocalDateTime) {
            d = ((LocalDateTime) value).toLocalDate();
        } else if (value instanceof String) {
            String s = (String) value;
            LocalDate parsed = null;
            try { parsed = LocalDate.parse(s); } catch (Exception ignore) {}
            if (parsed == null) {
                try { parsed = LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/uuuu")); }
                catch (Exception ignore) {}
            }
            if (parsed == null) throw new JspException("Unsupported date string: " + s);
            d = parsed;
        } else {
            throw new JspException("Unsupported type: " + value.getClass());
        }

        String out = String.format("ngay %02d thang %02d nam %04d",
                d.getDayOfMonth(), d.getMonthValue(), d.getYear());
        JspWriter w = getJspContext().getOut();
        w.print(out);
    }
}
