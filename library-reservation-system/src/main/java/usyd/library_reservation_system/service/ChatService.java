package usyd.library_reservation_system.library_reservation_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import usyd.library_reservation_system.library_reservation_system.model.Book;
import usyd.library_reservation_system.library_reservation_system.model.Label;
import usyd.library_reservation_system.library_reservation_system.repository.BookRepository;
import usyd.library_reservation_system.library_reservation_system.repository.LabelRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final RestTemplate restTemplate;
    private final BookRepository bookRepository;
    private final LabelRepository labelRepository;
    
    @Value("${openai.api.key:your-api-key-here}")
    private String apiKey;
    
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Autowired
    public ChatService(BookRepository bookRepository, LabelRepository labelRepository) {
        this.restTemplate = new RestTemplate();
        this.bookRepository = bookRepository;
        this.labelRepository = labelRepository;
    }

    public String getChatResponse(String userMessage) {
        try {
            String lowerMessage = userMessage.toLowerCase().trim();
            
            // Check if this is a book recommendation request
            if (isRecommendationRequest(lowerMessage)) {
                return getBookRecommendations(userMessage, lowerMessage);
            }
            
            // Other types of requests
            return getFriendlyResponse(userMessage, lowerMessage);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "😅 Oops! I encountered a small issue. Could you try again? I'm here to help!";
        }
    }
    
    /**
     * Check if this is a book recommendation request
     */
    private boolean isRecommendationRequest(String lowerMessage) {
        return lowerMessage.contains("recommend") || 
               lowerMessage.contains("recommend books") ||
               lowerMessage.contains("recommend some") ||
               lowerMessage.contains("what books") ||
               lowerMessage.contains("suggest books") ||
               lowerMessage.contains("good books") ||
               lowerMessage.contains("any books");
    }
    
    /**
     * Get book recommendations from database (with keyword support)
     */
    private String getBookRecommendations(String userMessage, String lowerMessage) {
        try {
            List<Book> recommendations;
            String categoryName = null;
            
            // Detect keywords and find corresponding category
            List<String> allLabels = labelRepository.findAll().stream()
                .map(Label::getLabelName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
            
            // Check if user message contains category keywords
            Optional<String> matchedLabel = allLabels.stream()
                .filter(label -> lowerMessage.contains(label))
                .findFirst();
            
            if (matchedLabel.isPresent()) {
                // Found category, recommend by category
                categoryName = matchedLabel.get();
                Optional<Label> label = labelRepository.findByLabelName(categoryName);
                if (label.isPresent()) {
                    List<Book> booksByLabel = bookRepository.findByLabelId(label.get().getLabelId());
                    // Sort by favorite count
                    recommendations = booksByLabel.stream()
                        .sorted((a, b) -> Integer.compare(b.getNumFavorite(), a.getNumFavorite()))
                        .limit(5)
                        .collect(Collectors.toList());
                } else {
                    recommendations = getDefaultRecommendations();
                }
            } else {
                // No category specified, recommend popular books
                recommendations = getDefaultRecommendations();
            }
            
            if (recommendations.isEmpty()) {
                if (categoryName != null) {
                    return String.format("😊 Sorry, I couldn't find any books in the '%s' category. " +
                        "Would you like me to recommend some popular books instead? " +
                        "Just ask me to 'recommend some books'!", categoryName);
                }
                return "📚 Oops! Our collection is currently empty. " +
                    "Please contact our administrators to add more books.";
            }
            
            // Build friendly recommendation message
            StringBuilder response = new StringBuilder();
            response.append("🌟 ");
            if (categoryName != null) {
                response.append(String.format("Here are some great '%s' books I found:\n\n", categoryName));
            } else {
                response.append("Here are some popular books I'd recommend:\n\n");
            }
            
            int index = 1;
            for (Book book : recommendations) {
                String emoji = index == 1 ? "🥇" : index == 2 ? "🥈" : index == 3 ? "🥉" : "📖";
                String availability = book.getQuantity() > 0 ? "✅ Available Now" : "⏳ Reserved";
                
                response.append(String.format("%s %d. **%s**\n", emoji, index++, book.getBookName()));
                response.append(String.format("   👤 Author: %s\n", book.getAuthor()));
                response.append(String.format("   📊 Status: %s\n", availability));
                
                if (book.getDescription() != null && !book.getDescription().isEmpty()) {
                    String shortDesc = book.getDescription().length() > 70 
                        ? book.getDescription().substring(0, 70) + "..." 
                        : book.getDescription();
                    response.append(String.format("   📝 About: %s\n", shortDesc));
                }
                
                if (book.getNumFavorite() != null && book.getNumFavorite() > 0) {
                    String starLevel = book.getNumFavorite() > 10 ? "⭐⭐⭐" : 
                                     book.getNumFavorite() > 5 ? "⭐⭐" : "⭐";
                    response.append(String.format("   %s Loved by %d readers!\n", starLevel, book.getNumFavorite()));
                }
                response.append("\n");
            }
            
            response.append("💡 Tip: Click any book to see details and reserve it!\n");
            response.append("🎯 Want a different category? Just ask me to 'recommend some [category] books'");
            
            return response.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
            return "😅 Sorry! I had trouble finding books. " +
                "Could you try again? I'm here to help!";
        }
    }
    
    /**
     * Get default recommendations (popular books)
     */
    private List<Book> getDefaultRecommendations() {
        List<Book> popularBooks = bookRepository.findMostPopularBooks();
        return popularBooks.stream()
                .limit(5)
                .collect(Collectors.toList());
    }
    
    /**
     * Friendly response handling
     */
    private String getFriendlyResponse(String userMessage, String lowerMessage) {
        // Greetings
        if (lowerMessage.contains("hello") || lowerMessage.contains("hi") || 
            lowerMessage.contains("hey") || lowerMessage.contains("greetings")) {
            return "👋 Hi there! I'm your friendly library AI assistant!\n\n" +
                   "I can help you with:\n" +
                   "✨ Book recommendations - ask 'recommend some books'\n" +
                   "🎯 Category-specific books - ask 'recommend some [category] books'\n" +
                   "📖 Learn how to reserve books\n" +
                   "🔍 Search for specific titles\n" +
                   "❓ General questions about our library\n\n" +
                   "What can I do for you today? 😊";
        }
        
        // Help requests
        else if (lowerMessage.contains("help") || lowerMessage.contains("assist") || 
                 lowerMessage.contains("what can you do")) {
            return "🎯 I'm here to help! Here's what I can do:\n\n" +
                   "📚 **Book Recommendations**\n" +
                   "   • Ask: 'recommend some books' for popular books\n" +
                   "   • Or specify: 'recommend some science books'\n\n" +
                   "🔍 **Search Books**\n" +
                   "   • Use the search bar at the top to find books by name, author, or keywords\n\n" +
                   "📖 **Reservations**\n" +
                   "   • Learn how to reserve and pick up books\n\n" +
                   "❓ **Questions**\n" +
                   "   • Ask me anything about our library system!\n\n" +
                   "Ready to get started? 😊";
        }
        
        // Reservation related
        else if (lowerMessage.contains("reserve") || lowerMessage.contains("reservation") ||
                 lowerMessage.contains("subscribe") || lowerMessage.contains("booking") ||
                 lowerMessage.contains("borrow") || lowerMessage.contains("checkout")) {
            return "📖 Great question about reservations!\n\n" +
                   "Here's how it works:\n" +
                   "1️⃣ Browse books and find one you like\n" +
                   "2️⃣ Click on the book to see details\n" +
                   "3️⃣ Click 'Subscribe' button to reserve\n" +
                   "4️⃣ You'll get notified when it's ready!\n\n" +
                   "💡 The status will show 'Available' when ready for pickup.\n\n" +
                   "Need book recommendations? Just ask me to 'recommend some books'! 😊";
        }
        
        // Search related
        else if (lowerMessage.contains("search") || lowerMessage.contains("lookup") ||
                 lowerMessage.contains("find") || lowerMessage.contains("look for") ||
                 lowerMessage.contains("how to find")) {
            return "🔍 Let me help you find books!\n\n" +
                   "You can search in two ways:\n\n" +
                   "1️⃣ **Use the search bar** at the top of the page:\n" +
                   "   • Type book names\n" +
                   "   • Type author names\n" +
                   "   • Use keywords\n\n" +
                   "2️⃣ **Ask me for recommendations**:\n" +
                   "   • 'recommend some books' - Popular books\n" +
                   "   • 'recommend some [category] books' - By category\n\n" +
                   "Try searching now! 🎯";
        }
        
        // Popular books
        else if (lowerMessage.contains("popular") || lowerMessage.contains("hot") ||
                 lowerMessage.contains("trending") || lowerMessage.contains("most read")) {
            return getBookRecommendations(userMessage, lowerMessage);
        }
        
        // Category inquiry
        else if (lowerMessage.contains("category") || lowerMessage.contains("type") ||
                 lowerMessage.contains("categories") || lowerMessage.contains("classification")) {
            List<String> allLabels = labelRepository.findAll().stream()
                .map(Label::getLabelName)
                .collect(Collectors.toList());
            
            if (allLabels.isEmpty()) {
                return "📂 Currently, there are no categories set up.\n" +
                       "But you can still browse all our books!\n\n" +
                       "Ask me to 'recommend some books' to see popular ones! 😊";
            }
            
            StringBuilder response = new StringBuilder("📂 Here are our book categories:\n\n");
            for (int i = 0; i < allLabels.size(); i++) {
                response.append(String.format("%d. %s\n", i + 1, allLabels.get(i)));
            }
            response.append("\n💡 Want books from a specific category? Try:\n");
            response.append("   'recommend some " + allLabels.get(0) + " books'");
            
            return response.toString();
        }
        
        // Default response - friendly fallback
        else {
            return "😊 I'm here to help!\n\n" +
                   "Here's what you can ask me:\n\n" +
                   "📚 **Book Recommendations**\n" +
                   "   • 'recommend some books' - Popular books\n" +
                   "   • 'recommend some [category] books' - By category\n\n" +
                   "🔍 **Search**\n" +
                   "   • 'How to search for books?'\n\n" +
                   "📖 **Reservations**\n" +
                   "   • 'How do I reserve a book?'\n\n" +
                   "💬 **General Help**\n" +
                   "   • 'What can you help me with?'\n\n" +
                   "What would you like to know? 😊";
        }
    }
}

