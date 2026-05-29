package com.airtribe.librarymanagement.service;

import com.airtribe.librarymanagement.entity.*;
import com.airtribe.librarymanagement.observerPattern.Observer;
import com.airtribe.librarymanagement.repository.InventoryRepository;
import com.airtribe.librarymanagement.repository.PatronRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import com.airtribe.librarymanagement.exception.*;

public class LibraryService {
    private final InventoryRepository inventoryRepo;
    private final PatronRepository patronRepo;
    private final List<Observer> observers = new CopyOnWriteArrayList<>();

    public LibraryService(InventoryRepository inventoryRepo, PatronRepository patronRepo) {
        this.inventoryRepo = inventoryRepo;
        this.patronRepo = patronRepo;
    }

    public void addBookCopy(String barcode, String isbn, String title, String author, int publicationYear) {
        Book metadata = inventoryRepo.findByIsbn(isbn);
        if (metadata == null) {
            metadata = new Book(title, author, isbn, publicationYear);
            inventoryRepo.saveBookMetaData(metadata);
        }

        BookItem newItem = new BookItem(barcode, metadata);
        inventoryRepo.saveBookItem(newItem);
    }

    public void removeCopy(String barcode) {
        inventoryRepo.removeItemByBarcode(barcode);
    }

    public void updateBookMetadata(String isbn, String updatedTitle, String updatedAuthor, int updatedYear) {
        Book metadata = inventoryRepo.findByIsbn(isbn);
        if (metadata == null) {
            throw new BookNotFoundException("Book metadata with ISBN " + isbn + " not found.");
        }
        // Re-instantiate/overwrite the metadata mapping definition
        Book updatedMetadata = new Book(updatedTitle, updatedAuthor, isbn, updatedYear);
        inventoryRepo.saveBookMetaData(updatedMetadata);
    }

    public void addBook(String isbn, String title, String author, int publicationYear) {
        Book metadata = inventoryRepo.findByIsbn(isbn);
        if (metadata == null) {
            metadata = new Book(title, author, isbn, publicationYear);
            inventoryRepo.saveBookMetaData(metadata);
        }
    }

    public void removeBook(String isbn) {
        Book metadata = inventoryRepo.findByIsbn(isbn);
        if (metadata == null) {
            throw new BookNotFoundException("Book with ISBN " + isbn + " not found.");
        }

        // Check if any copies of the book are currently BORROWED
        for (BookItem item : inventoryRepo.getAllItems()) {
            if (item.getMetadata().getIsbn().equals(isbn)) {
                if (item.getStatus() == BookStatus.BORROWED) {
                    throw new BookNotAvailableException("Cannot remove book with ISBN " + isbn + " because some copies are currently borrowed.");
                }
            }
        }

        // If no copies are borrowed, clean up all copies of this book
        for (BookItem item : inventoryRepo.getAllItems()) {
            if (item.getMetadata().getIsbn().equals(isbn)) {
                inventoryRepo.removeItemByBarcode(item.getBarcode());
            }
        }

        // Remove catalog metadata
        inventoryRepo.removeBookMetaData(isbn);
    }

    public List<Book> searchBooks(String query) {
        String lowerQuery = query.toLowerCase();
        List<Book> results = new ArrayList<>();
        for (Book book : inventoryRepo.getAllBooks()) {
            if (book.getIsbn().toLowerCase().contains(lowerQuery) ||
                book.getTitle().toLowerCase().contains(lowerQuery) ||
                book.getAuthor().toLowerCase().contains(lowerQuery)) {
                results.add(book);
            }
        }
        return results;
    }

    public List<BookItem> searchBookItems(String query) {
        String lowerQuery = query.toLowerCase();
        List<BookItem> results = new ArrayList<>();

        for (BookItem item : inventoryRepo.getAllItems()) {
            if (item.getBarcode().toLowerCase().contains(lowerQuery) ||
                    item.getMetadata().getIsbn().toLowerCase().contains(lowerQuery) ||
                    item.getMetadata().getTitle().toLowerCase().contains(lowerQuery) ||
                    item.getMetadata().getAuthor().toLowerCase().contains(lowerQuery)) {
                results.add(item);
            }
        }
        return results;
    }

    public List<BookItem> getAvailableItems() {
        List<BookItem> available = new ArrayList<>();
        for (BookItem item : inventoryRepo.getAllItems()) {
            if (item.isAvailable()) {
                available.add(item);
            }
        }
        return available;
    }

    public List<BookItem> getBorrowedItems() {
        List<BookItem> borrowed = new ArrayList<>();
        for (BookItem item : inventoryRepo.getAllItems()) {
            if (item.getStatus() == BookStatus.BORROWED) {
                borrowed.add(item);
            }
        }
        return borrowed;
    }

    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    private void notifyCheckout(BookItem item, Patron patron) {
        for (Observer observer : observers) {
            observer.onBookCheckedOut(item, patron);
        }
    }

    private void notifyReturn(BookItem item, Patron patron) {
        for (Observer observer : observers) {
            observer.onBookReturned(item, patron);
        }
    }

    public synchronized void checkoutBookItem(String barcode, String patronId) {
        BookItem item = inventoryRepo.findBookByBarCode(barcode);
        if (item == null) {
            throw new BookNotFoundException("Book copy with Barcode " + barcode + " not found.");
        }
        Patron patron = patronRepo.findById(patronId);
        if (patron == null) {
            throw new PatronNotFoundException("Patron with ID " + patronId + " not found.");
        }
        if (!item.isAvailable()) {
            throw new BookNotAvailableException("Book copy with Barcode " + barcode + " is already borrowed or reserved.");
        }

        item.setStatus(BookStatus.BORROWED);

        String recordId = UUID.randomUUID().toString();
        LendingRecord record = new LendingRecord(recordId, barcode, patronId);
        patron.addLendingRecord(record);
        notifyCheckout(item, patron);
    }

    public synchronized void returnBookItem(String barcode, String patronId) {
        BookItem item = inventoryRepo.findBookByBarCode(barcode);
        if (item == null) {
            throw new BookNotFoundException("Book copy with Barcode " + barcode + " not found.");
        }
        Patron patron = patronRepo.findById(patronId);
        if (patron == null) {
            throw new PatronNotFoundException("Patron with ID " + patronId + " not found.");
        }
        if (item.getStatus() != BookStatus.BORROWED) {
            throw new BookNotBorrowedException("Book copy with Barcode " + barcode + " is not currently borrowed.");
        }

        for (LendingRecord record : patron.getBorrowingHistory()) {
            if (record.getBarcode().equals(barcode) && record.getReturnDate() == null) {
                record.markReturned();
                item.setStatus(BookStatus.AVAILABLE);
                notifyReturn(item, patron);
                return;
            }
        }
        throw new BookNotBorrowedException("Book copy with Barcode " + barcode + " was not checked out by patron " + patronId + ".");
    }

    public void registerPatron(String patronId, String name, String email) {
        Patron patron = new Patron(patronId, name, email);
        patronRepo.save(patron);
    }

    public void updatePatronDetails(String patronId, String name, String email) {
        Patron patron = patronRepo.findById(patronId);
        if (patron == null) {
            throw new PatronNotFoundException("Patron with ID " + patronId + " not found.");
        }
        patron.setName(name);
        patron.setEmail(email);
    }

    public List<LendingRecord> getPatronBorrowingHistory(String patronId) {
        Patron patron = patronRepo.findById(patronId);
        if (patron != null) {
            return patron.getBorrowingHistory();
        }
        return new ArrayList<>();
    }
}
