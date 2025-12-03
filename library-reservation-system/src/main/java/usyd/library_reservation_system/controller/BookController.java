package usyd.library_reservation_system.library_reservation_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import usyd.library_reservation_system.library_reservation_system.model.Book;
import usyd.library_reservation_system.library_reservation_system.service.BookService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*",
        allowedHeaders = {"*", "X-USER-ID"},
        exposedHeaders = {"X-USER-ID"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class BookController {

    @Autowired
    private BookService bookService;
    @Autowired
    private usyd.library_reservation_system.library_reservation_system.service.FavoriteService favoriteService;


    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Integer id) {
        try {
            Optional<Book> book = bookService.getBookById(id);
            if (book.isPresent()) {
                return ResponseEntity.ok(book.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        try {
            Book createdBook = bookService.createBook(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Integer id, @RequestBody Book bookDetails) {
        try {
            Book updatedBook = bookService.updateBook(id, bookDetails);
            if (updatedBook != null) {
                return ResponseEntity.ok(updatedBook);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer id) {
        try {
            boolean deleted = bookService.deleteBook(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String keyword) {
        try {
            List<Book> books = bookService.searchBooks(keyword);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/search/name")
    public ResponseEntity<List<Book>> searchBooksByName(@RequestParam String name) {
        try {
            List<Book> books = bookService.searchBooksByName(name);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/search/author")
    public ResponseEntity<List<Book>> searchBooksByAuthor(@RequestParam String author) {
        try {
            List<Book> books = bookService.searchBooksByAuthor(author);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/label/{labelId}")
    public ResponseEntity<List<Book>> getBooksByLabelId(@PathVariable Integer labelId) {
        try {
            List<Book> books = bookService.getBooksByLabelId(labelId);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        try {
            List<Book> books = bookService.getAvailableBooks();
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/popular")
    public ResponseEntity<List<Book>> getMostPopularBooks() {
        try {
            List<Book> books = bookService.getMostPopularBooks();
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/most-reserved")
    public ResponseEntity<List<Book>> getMostReservedBooks() {
        try {
            List<Book> books = bookService.getMostReservedBooks();
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PatchMapping("/{id}/quantity")
    public ResponseEntity<Book> updateBookQuantity(@PathVariable Integer id, @RequestBody QuantityRequest request) {
        try {
            Book updatedBook = bookService.updateBookQuantity(id, request.getQuantity());
            if (updatedBook != null) {
                return ResponseEntity.ok(updatedBook);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> incrementFavoriteCount(
            @PathVariable Integer id,
            jakarta.servlet.http.HttpServletRequest req
    ) {
        String h = req.getHeader("X-USER-ID");
        if (h == null || !h.matches("\\d+")) {
            return ResponseEntity.status(401)
                    .body(java.util.Map.of("error","UNAUTHORIZED","message","Missing X-USER-ID header"));
        }
        Integer userId = Integer.valueOf(h);

        try {
            favoriteService.add(userId, id);   // Using field-injected service
            return ResponseEntity.ok().build();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error","BAD_REQUEST",
                            "message","Invalid userId or bookId (FK/constraint failed)"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(java.util.Map.of("error","INTERNAL_ERROR","message", e.getMessage()));
        }
    }


    @PostMapping("/{id}/reservation")
    public ResponseEntity<Book> incrementReservationCount(@PathVariable Integer id) {
        try {
            Book updatedBook = bookService.incrementReservationCount(id);
            if (updatedBook != null) {
                return ResponseEntity.ok(updatedBook);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    public static class QuantityRequest {
        private Integer quantity;

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }


    @GetMapping("/search-by-label")
    public ResponseEntity<List<BookService.BookSimpleDto>> searchByLabel(@RequestParam("q") String q) {
        try {
            var result = bookService.searchByLabelName(q);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

//    @PostMapping("/{id}/favorite")
//    public ResponseEntity<?> addFavorite(@PathVariable Integer id,
//                                         javax.servlet.http.HttpServletRequest req,
//                                         usyd.library_reservation_system.library_reservation_system.service.FavoriteService favoriteService) {
//        Integer userId = Integer.valueOf(req.getHeader("X-USER-ID")); // Consistent with FavoriteController
//        favoriteService.add(userId, id);
//        return ResponseEntity.ok().build();
//    }

    /**
     * Upload book cover image
     */
    @PostMapping("/cover")
    public ResponseEntity<Map<String, String>> uploadCover(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        
        System.out.println("📤 Received upload request - File: " + file.getOriginalFilename() + ", Size: " + file.getSize());
        
        if (file == null || file.isEmpty()) {
            System.out.println("❌ Error: No file uploaded");
            response.put("error", "No file uploaded");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "unknown";
            }
            System.out.println("📝 Original filename: " + originalFilename);
            
            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            System.out.println("📝 Generated filename: " + filename);
            
            // Try multiple possible paths
            String webappPath = null;
            String userDir = System.getProperty("user.dir");
            System.out.println("🔍 Current working directory: " + userDir);
            
            String[] possiblePaths = {
                userDir + File.separator + "src" + File.separator + "main" + File.separator + "webapp",
                userDir + File.separator + "library-reservation-system" + File.separator + "src" + File.separator + "main" + File.separator + "webapp",
                "src/main/webapp",
                "library-reservation-system/src/main/webapp"
            };
            
            for (String path : possiblePaths) {
                File testPath = new File(path);
                System.out.println("🔍 Testing path: " + path + " -> " + (testPath.exists() ? "EXISTS" : "NOT FOUND"));
                if (testPath.exists()) {
                    webappPath = path;
                    break;
                }
            }
            
            if (webappPath == null) {
                webappPath = userDir;
                System.out.println("⚠️ No webapp path found, using: " + webappPath);
            }
            
            System.out.println("📂 Using webapp path: " + webappPath);
            
            // Define upload directory - direct path to covers
            String uploadDir = webappPath + File.separator + "resource" + File.separator + 
                             "img" + File.separator + "covers" + File.separator;
            
            System.out.println("📂 Upload directory: " + uploadDir);
            
            File uploadPath = new File(uploadDir);
            
            // Create directory if it doesn't exist
            if (!uploadPath.exists()) {
                boolean created = uploadPath.mkdirs();
                System.out.println("📁 Directory created: " + created);
            }
            
            // Save file
            Path filePath = Paths.get(uploadDir + filename);
            System.out.println("💾 Saving file to: " + filePath.toAbsolutePath());
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return the path in the same format as existing books: /img/covers/filename.jpg
            String avatarPath = "/img/covers/" + filename;
            response.put("filename", avatarPath);
            response.put("success", "true");
            
            System.out.println("✅ Cover uploaded successfully: " + avatarPath);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Upload failed: " + e.getMessage());
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Unexpected error: " + e.getMessage());
            response.put("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
