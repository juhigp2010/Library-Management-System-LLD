package com.airtribe.librarymanagement.corefunctionality;

import com.airtribe.librarymanagement.observerPattern.EmailNotificationService;
import com.airtribe.librarymanagement.observerPattern.SmsObserverService;
import com.airtribe.librarymanagement.repository.InventoryRepository;
import com.airtribe.librarymanagement.repository.PatronRepository;
import com.airtribe.librarymanagement.service.LibraryService;
import com.airtribe.librarymanagement.exception.*;

public class Main {
    public static void main(String[] args) {
        // Initialize Storage and Core Services
        InventoryRepository inventoryRepository = new InventoryRepository();
        PatronRepository patronRepository = new PatronRepository();
        LibraryService library = new LibraryService(inventoryRepository, patronRepository);

        // Attach Observers
        library.registerObserver(new EmailNotificationService());
        library.registerObserver(new SmsObserverService());

        // Setup Test Data
        library.addBookCopy("BC-777", "978-0134685991", "Effective Java", "Joshua Bloch", 2018);
        library.registerPatron("P-999", "Sarah Connor", "sarah@sky.net");

        System.out.println("--- Triggering Checkout Event ---");
        try {
            library.checkoutBookItem("BC-777", "P-999");
        } catch (LibraryException e) {
            System.err.println("Checkout failed: " + e.getMessage());
        }

        System.out.println("\n--- Triggering Return Event ---");
        try {
            library.returnBookItem("BC-777", "P-999");
        } catch (LibraryException e) {
            System.err.println("Return failed: " + e.getMessage());
        }

        System.out.println("\n--- Testing Exception scenarios ---");

        // Scenario 1: Book Not Found Exception
        try {
            System.out.println("Attempting checkout with invalid barcode:");
            library.checkoutBookItem("BC-INVALID", "P-999");
        } catch (BookNotFoundException e) {
            System.out.println("Caught Expected Exception: " + e.getMessage());
        }

        // Scenario 2: Patron Not Found Exception
        try {
            System.out.println("Attempting checkout with invalid patron ID:");
            library.checkoutBookItem("BC-777", "P-INVALID");
        } catch (PatronNotFoundException e) {
            System.out.println("Caught Expected Exception: " + e.getMessage());
        }

        // Scenario 3: Book Not Available Exception
        try {
            System.out.println("Checking out book first:");
            library.checkoutBookItem("BC-777", "P-999");
            System.out.println("Attempting checkout of already checked out book:");
            library.checkoutBookItem("BC-777", "P-999");
        } catch (BookNotAvailableException e) {
            System.out.println("Caught Expected Exception: " + e.getMessage());
        }

        // Scenario 4: Book Not Borrowed Exception
        try {
            System.out.println("Attempting return of a book that is already available:");
            library.returnBookItem("BC-777", "P-999"); // Wait, we just checked it out in the try block above, let's
                                                       // return it once
            library.returnBookItem("BC-777", "P-999"); // This second return should fail
        } catch (BookNotBorrowedException e) {
            System.out.println("Caught Expected Exception: " + e.getMessage());
        }

        System.out.println("\n--- Testing Book Management & Inventory Enhancements ---");

        // Test 1: Add book catalog metadata-only
        library.addBook("978-0596007126", "Head First Design Patterns", "Elisabeth Freeman", 2004);
        System.out.println("Added Book 'Head First Design Patterns' to catalog.");

        // Test 2: Search catalog
        System.out.println("Searching catalog for 'Design Patterns':");
        for (var book : library.searchBooks("Design Patterns")) {
            System.out.println(String.format("Found Book: %s by %s (ISBN: %s, Year: %d)",
                    book.getTitle(), book.getAuthor(), book.getIsbn(), book.getPublicationYear()));
        }

        // Test 3: Attempt to remove a Book while its copies are borrowed
        System.out.println("Adding copy of Effective Java and checking it out:");
        library.addBookCopy("BC-888", "978-0134685991", "Effective Java", "Joshua Bloch", 2018);
        library.checkoutBookItem("BC-888", "P-999");

        try {
            System.out.println("Attempting to remove 'Effective Java' book catalog entry (ISBN: 978-0134685991):");
            library.removeBook("978-0134685991");
        } catch (BookNotAvailableException e) {
            System.out.println("Caught Expected Exception: " + e.getMessage());
        }

        // Clean up: return the copy and remove the book catalog entry
        System.out.println("Returning copy 'BC-888':");
        library.returnBookItem("BC-888", "P-999");

        System.out.println("Removing book catalog entry after returning all copies:");
        library.removeBook("978-0134685991");
        System.out.println("Removal successful.");

        // Verify book is removed from catalog search
        System.out.println("Searching catalog for 'Effective Java' after removal:");
        var searchResults = library.searchBooks("Effective Java");
        System.out.println("Search results size: " + searchResults.size());
    }
}