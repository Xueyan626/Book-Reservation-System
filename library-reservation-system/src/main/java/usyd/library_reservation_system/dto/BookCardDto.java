package usyd.library_reservation_system.library_reservation_system.dto;

public record BookCardDto(
        Integer bookId,
        String bookName,
        String author,
        String avatar,
        Integer labelId
) {}
