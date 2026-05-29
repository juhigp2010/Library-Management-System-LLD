package com.airtribe.librarymanagement.observerPattern;

import com.airtribe.librarymanagement.entity.BookItem;
import com.airtribe.librarymanagement.entity.Patron;

public interface Observer {
    void onBookCheckedOut(BookItem item, Patron patron);
    void onBookReturned(BookItem item, Patron patron);
}
