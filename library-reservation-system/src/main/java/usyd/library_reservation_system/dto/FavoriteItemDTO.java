package usyd.library_reservation_system.library_reservation_system.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FavoriteItemDTO {
    private Integer bookId;
    private String  bookName;
    private String  author;
    private String  labelName;
    private String  avatar;     // Cover image
}
