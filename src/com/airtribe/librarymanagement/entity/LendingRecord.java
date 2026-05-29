package com.airtribe.librarymanagement.entity;

import java.time.LocalDateTime;

public class LendingRecord {
    private final String recordId;
    private final String barcode;
    private final String patronId;
    private final LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
    public LendingRecord(String recordId, String barcode, String patronId) {
        this.recordId = recordId;
        this.barcode = barcode;
        this.patronId = patronId;
        this.checkoutDate = LocalDateTime.now();
        this.returnDate = null;
    }

    public void markReturned() {
        this.returnDate = LocalDateTime.now();
    }

    public String getRecordId() {
        return recordId;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getPatronId() {
        return patronId;
    }

    public LocalDateTime getCheckoutDate() {
        return checkoutDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }
}
