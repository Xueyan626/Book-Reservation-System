package usyd.library_reservation_system.library_reservation_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import usyd.library_reservation_system.library_reservation_system.model.Book;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    // Fuzzy search by book name
    List<Book> findByBookNameContaining(String bookName);

    // Fuzzy search by author name
    List<Book> findByAuthorContaining(String author);

    // Query books by label ID
    List<Book> findByLabelId(Integer labelId);

    // Query books with stock available
    List<Book> findByQuantityGreaterThan(Integer quantity);

    // Custom query: search book name and author by keyword
    @Query("SELECT b FROM Book b WHERE b.bookName LIKE %:keyword% OR b.author LIKE %:keyword%")
    List<Book> searchByKeyword(@Param("keyword") String keyword);

    // Get most popular books (sorted by favorite count)
    @Query("SELECT b FROM Book b ORDER BY b.numFavorite DESC")
    List<Book> findMostPopularBooks();

    // Get most reserved books
    @Query("SELECT b FROM Book b ORDER BY b.numReservation DESC")
    List<Book> findMostReservedBooks();

    // Fuzzy search books by label name (exact match first, then partial match)
    @Query(value = """
        SELECT b.book_id AS bookId, b.book_name AS bookName
        FROM book b
        JOIN label l ON l.label_id = b.label_id
        WHERE l.label_name LIKE CONCAT('%', :q, '%')
        ORDER BY 
          (l.label_name = :q) DESC,      -- Exact match first
          l.label_name ASC,              -- Then by label name (optional)
          b.book_name ASC                -- Then by book name (optional)
        """, nativeQuery = true)
    List<usyd.library_reservation_system.library_reservation_system.repository.BookSimpleProjection>
    searchBooksByLabelNameWithPriority(@Param("q") String q);

}