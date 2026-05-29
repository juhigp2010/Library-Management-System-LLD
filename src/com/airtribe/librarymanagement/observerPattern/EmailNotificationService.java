package com.airtribe.librarymanagement.observerPattern;

import com.airtribe.librarymanagement.entity.BookItem;
import com.airtribe.librarymanagement.entity.Patron;

public class EmailNotificationService implements Observer{
    @Override
    public void onBookCheckedOut(BookItem item, Patron patron) {
        System.out.println(String.format("[EMAIL SENT to %s]: Successfully checked out '%s' (Barcode: %s).",
                patron.getEmail(), item.getMetadata().getTitle(), item.getBarcode()));
    }

    @Override
    public void onBookReturned(BookItem item, Patron patron) {
        System.out.println(String.format("[EMAIL SENT to %s]: Thank you for returning '%s'.",
                patron.getEmail(), item.getMetadata().getTitle()));
    }
}
