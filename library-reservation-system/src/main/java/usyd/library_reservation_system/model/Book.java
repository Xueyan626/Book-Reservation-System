package usyd.library_reservation_system.library_reservation_system.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "book")
@Data
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer bookId;

    @Column(name = "book_name", nullable = false, length = 200)
    private String bookName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Column(name = "description")
    private String description;

    @Column(name = "num_favorite", nullable = false)
    private Integer numFavorite;

    @Column(name = "num_reservation", nullable = false)
    private Integer numReservation;

    @Column(name = "label_id", nullable = false)
    private Integer labelId;

    @Column(name = "avatar")
    private String avatar;
}

