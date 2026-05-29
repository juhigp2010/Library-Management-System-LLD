package com.airtribe.librarymanagement.entity;

public class BookItem {
    public BookItem(String barcode, Book metadata) {
        this.barcode = barcode;
        this.metadata = metadata;
        this.status = BookStatus.AVAILABLE;
    }

    private final String barcode;
    private final Book metadata;
    private BookStatus status;

    public synchronized BookStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(BookStatus status) {
        this.status = status;
    }

    public synchronized boolean isAvailable() {
        return this.status == BookStatus.AVAILABLE;
    }

    public String getBarcode() {
        return barcode;
    }

    public Book getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return String.format("Copy [Barcode: %s, Title: %s, Status: %s]",
                barcode, metadata.getTitle(), status);
    }
}