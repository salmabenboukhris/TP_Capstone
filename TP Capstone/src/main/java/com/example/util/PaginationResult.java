package com.example.util;

import java.util.List;

public class PaginationResult<T> {
    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final int totalPages;
    private final long totalItems;

    public PaginationResult(List<T> items, int currentPage, int pageSize, int totalPages, long totalItems) {
        this.items = items;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }

    public List<T> getItems() { return items; }
    public int getCurrentPage() { return currentPage; }
    public int getPageSize() { return pageSize; }
    public int getTotalPages() { return totalPages; }
    public long getTotalItems() { return totalItems; }
    public boolean hasNext() { return currentPage < totalPages; }
    public boolean hasPrevious() { return currentPage > 1; }
}
