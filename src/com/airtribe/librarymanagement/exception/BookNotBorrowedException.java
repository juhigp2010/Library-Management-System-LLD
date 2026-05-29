package com.airtribe.librarymanagement.exception;

public class BookNotBorrowedException extends LibraryException {
    public BookNotBorrowedException(String message) {
        super(message);
    }
}
