package com.airtribe.librarymanagement.observerPattern;

import com.airtribe.librarymanagement.entity.BookItem;
import com.airtribe.librarymanagement.entity.Patron;

public class SmsObserverService implements Observer{
    @Override
    public void onBookCheckedOut(BookItem item, Patron patron) {
        System.out.println(String.format("[SMS SENT to %s]: Tx Confirmed! Barcode %s is now yours.",
                patron.getName(), item.getBarcode()));
    }

    @Override
    public void onBookReturned(BookItem item, Patron patron) {
        System.out.println(String.format("[SMS SENT to %s]: Book return logged successfully.",
                patron.getName()));
    }
}
