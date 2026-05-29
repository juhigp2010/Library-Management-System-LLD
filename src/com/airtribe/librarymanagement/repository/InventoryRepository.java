package com.airtribe.librarymanagement.repository;

import com.airtribe.librarymanagement.entity.Book;
import com.airtribe.librarymanagement.entity.BookItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryRepository {
    private final Map<String, Book> booksByIsbn = new ConcurrentHashMap<>();
    private final Map<String, BookItem> itemsByBarcode = new ConcurrentHashMap<>();

    public void saveBookMetaData(Book book)
    {
        booksByIsbn.put(book.getIsbn(),book);
    }
    public Book findByIsbn(String isbn)
    {
        return booksByIsbn.get(isbn);
    }
    public void saveBookItem(BookItem item)
    {
        itemsByBarcode.put(item.getBarcode(), item);
    }
    public BookItem findBookByBarCode(String barcode)
    {
        return itemsByBarcode.get(barcode);
    }
    public void removeItemByBarcode(String barcode)
    {
        itemsByBarcode.remove(barcode);
    }
    public List<BookItem> getAllItems()
    {
        return new ArrayList<>(itemsByBarcode.values());
    }
    public void removeBookMetaData(String isbn)
    {
        booksByIsbn.remove(isbn);
    }
    public List<Book> getAllBooks()
    {
        return new ArrayList<>(booksByIsbn.values());
    }
}
