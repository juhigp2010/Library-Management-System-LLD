package com.airtribe.librarymanagement.entity;

import java.util.ArrayList;
import java.util.List;

public class Patron {
    private String name;
    private String email;
    private String patronId;
    private final List<LendingRecord> borrowHistory = new ArrayList<>();

    public Patron(String patronId, String name, String email) {
        this.patronId = patronId;
        this.name = name;
        this.email = email;
    }

    public String getPatronId() {
        return patronId;
    }

    public String getName() {
        return name;
    }

    public synchronized void addLendingRecord(LendingRecord record) {
        borrowHistory.add(record);
    }

    public synchronized List<LendingRecord> getBorrowingHistory() {
        return new ArrayList<>(borrowHistory);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
