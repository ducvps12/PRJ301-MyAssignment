package com.acme.leavemgmt.util;

public final class Pagination {
    private Pagination() {}

    public static String render(PageMeta m, String baseUrl) {
        int page = Math.max(1, m.page());
        int totalPages = m.totalPages();

        StringBuilder sb = new StringBuilder();
        sb.append("<nav class='pager'><ul>");

        int prev = Math.max(1, page - 1), next = Math.min(totalPages, page + 1);
        sb.append(li(link(baseUrl, prev, "&laquo;")));
        for (int i = Math.max(1, page - 2); i <= Math.min(totalPages, page + 2); i++) {
            String cls = (i == page) ? " class='active'" : "";
            sb.append("<li").append(cls).append(">")
              .append(link(baseUrl, i, String.valueOf(i))).append("</li>");
        }
        sb.append(li(link(baseUrl, next, "&raquo;")));
        sb.append("</ul></nav>");
        return sb.toString();
    }

    private static String link(String base, int p, String text) {
        String sep = base.contains("?") ? "&" : "?";
        return "<a href='"+base+sep+"page="+p+"'>"+text+"</a>";
    }
    private static String li(String x) { return "<li>"+x+"</li>"; }
}
