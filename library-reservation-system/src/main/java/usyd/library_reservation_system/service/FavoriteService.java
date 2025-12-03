package usyd.library_reservation_system.library_reservation_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usyd.library_reservation_system.library_reservation_system.model.Book;
import usyd.library_reservation_system.library_reservation_system.model.favorite.Favorite;
import usyd.library_reservation_system.library_reservation_system.model.favorite.FavoriteId;
import usyd.library_reservation_system.library_reservation_system.repository.BookRepository;
import usyd.library_reservation_system.library_reservation_system.repository.FavoriteRepository;
import usyd.library_reservation_system.library_reservation_system.dto.BookCardDto;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private BookRepository bookRepository;

    public record ToggleResult(Integer bookId, Integer userId, boolean favorited, Integer numFavorite) {}

    @Transactional
    public ToggleResult toggle(Integer userId, Integer bookId) {
        FavoriteId id = new FavoriteId(bookId, userId);
        boolean exists = favoriteRepository.existsById(id);

        // Update Book count
        Optional<Book> opt = bookRepository.findById(bookId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Book not found: " + bookId);
        Book book = opt.get();
        if (book.getNumFavorite() == null) book.setNumFavorite(0);

        if (!exists) {
            // Favorite: insert favorite + increment book favorite count
            Favorite f = Favorite.builder()
                    .id(id)
                    .createTime(LocalDateTime.now())
                    .build();
            favoriteRepository.save(f);

            book.setNumFavorite(book.getNumFavorite() + 1);
            bookRepository.save(book);

            return new ToggleResult(bookId, userId, true, book.getNumFavorite());
        } else {
            // Unfavorite: delete favorite + decrement book favorite count (not below 0)
            favoriteRepository.deleteById(id);

            int nf = Math.max(0, book.getNumFavorite() - 1);
            book.setNumFavorite(nf);
            bookRepository.save(book);

            return new ToggleResult(bookId, userId, false, nf);
        }
    }


    // 1) Check if favorited
    public boolean exists(Integer userId, Integer bookId) {
        return favoriteRepository.existsById(new FavoriteId(bookId, userId));
    }

    // 2) Add favorite (return directly if already exists)
    @Transactional
    public void add(Integer userId, Integer bookId) {
        var id = new FavoriteId(bookId, userId);
        if (favoriteRepository.existsById(id)) return;

        // Create new favorite
        var fav = usyd.library_reservation_system.library_reservation_system.model.favorite.Favorite.builder()
                .id(id)
                .createTime(java.time.LocalDateTime.now())
                .build();
        favoriteRepository.save(fav);

        // Sync book num_favorite +1
        bookRepository.findById(bookId).ifPresent(b -> {
            b.setNumFavorite((b.getNumFavorite() == null ? 0 : b.getNumFavorite()) + 1);
            bookRepository.save(b);
        });
    }

    // 3) Remove favorite (return if not exists)
    @Transactional
    public void remove(Integer userId, Integer bookId) {
        var id = new FavoriteId(bookId, userId);
        if (!favoriteRepository.existsById(id)) return;

        favoriteRepository.deleteById(id);

        // Sync num_favorite -1 (not below 0)
        bookRepository.findById(bookId).ifPresent(b -> {
            int nf = Math.max(0, (b.getNumFavorite() == null ? 0 : b.getNumFavorite()) - 1);
            b.setNumFavorite(nf);
            bookRepository.save(b);
        });
    }

    // 4) List books for "current user" (for Favorite.vue)
    @Transactional(readOnly = true)
    public java.util.List<Book> listBooksByUser(Integer userId) {
        var favorites = favoriteRepository.findByIdUserId(userId);
        var ids = favorites.stream().map(f -> f.getId().getBookId()).toList();
        return ids.isEmpty() ? java.util.List.of() : bookRepository.findAllById(ids);
    }

    // 4.1) Return "safe DTO" to frontend (avoid lazy loading/circular reference with entity serialization)
    @Transactional(readOnly = true)
    public java.util.List<BookCardDto> listBookCardsByUser(Integer userId) {
        var favorites = favoriteRepository.findByIdUserId(userId);
        var ids = favorites.stream().map(f -> f.getId().getBookId()).toList();
        if (ids.isEmpty()) return java.util.List.of();

        var books = bookRepository.findAllById(ids);
        return books.stream()
                .map(b -> new BookCardDto(
                        b.getBookId(),
                        b.getBookName(),
                        b.getAuthor(),
                        b.getAvatar(),  // Return as-is; frontend handles path sanitization
                        b.getLabelId()
                ))
                .toList();
    }

}
