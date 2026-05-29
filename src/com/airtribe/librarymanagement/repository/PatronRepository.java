package com.airtribe.librarymanagement.repository;

import com.airtribe.librarymanagement.entity.Patron;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PatronRepository {
    private final Map<String, Patron> patronsById = new ConcurrentHashMap<>();
    public void save(Patron patron) {
        patronsById.put(patron.getPatronId(), patron);
    }

    public Patron findById(String patronId) {
        return patronsById.get(patronId);
    }
}
