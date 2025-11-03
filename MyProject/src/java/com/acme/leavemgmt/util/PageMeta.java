package com.acme.leavemgmt.util;

public final class PageMeta {
    private final int page;      // trang hiện tại (>=1)
    private final int pageSize;  // số item mỗi trang (>=1)
    private final int total;     // tổng số item (>=0)

    public PageMeta(int page, int pageSize, int total) {
        int ps = pageSize <= 0 ? 10 : pageSize;
        int t  = Math.max(0, total);
        int tp = Math.max(1, (int) Math.ceil(t / (double) ps));
        int p  = Math.max(1, Math.min(page <= 0 ? 1 : page, tp));

        this.page = p;
        this.pageSize = ps;
        this.total = t;
    }

    /** Trang hiện tại (đã được clamp về [1..totalPages]) */
    public int page() { return page; }

    /** Kích thước trang */
    public int pageSize() { return pageSize; }

    /** Tổng số item */
    public int total() { return total; }

    /** Tổng số trang (>=1, kể cả khi total = 0) */
    public int totalPages() {
        return Math.max(1, (int) Math.ceil(total / (double) pageSize));
    }

    /** Trang trước (đã clamp >=1) */
    public int prev() { return Math.max(1, page - 1); }

    /** Trang sau (đã clamp <= totalPages) */
    public int next() { return Math.min(totalPages(), page + 1); }

    /** offset cho SQL: LIMIT ?,? (hoặc OFFSET ... FETCH ...) */
    public int offset() { return (page - 1) * pageSize; }

    /** chỉ số item đầu của trang hiện tại (1-based); 0 nếu không có item */
    public int fromIndex() { return total == 0 ? 0 : offset() + 1; }

    /** chỉ số item cuối của trang hiện tại (1-based); 0 nếu không có item */
    public int toIndex() { return total == 0 ? 0 : Math.min(total, offset() + pageSize); }

    public boolean hasPrev() { return page > 1; }
    public boolean hasNext() { return page < totalPages(); }

    @Override public String toString() {
        return "PageMeta{page=" + page + ", pageSize=" + pageSize +
               ", total=" + total + ", totalPages=" + totalPages() + "}";
    }
}
