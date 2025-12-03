package usyd.library_reservation_system.library_reservation_system.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import usyd.library_reservation_system.library_reservation_system.service.EmailOrPhoneAlreadyUsedException;
import usyd.library_reservation_system.library_reservation_system.service.InvalidCredentialsException;

import java.util.HashMap;
import java.util.Map;

@lombok.extern.slf4j.Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "VALIDATION_FAILED");
        var details = new HashMap<String, String>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> details.put(fe.getField(), fe.getDefaultMessage()));
        body.put("details", details);
        return body;
    }

    @ExceptionHandler(EmailOrPhoneAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleConflict(EmailOrPhoneAlreadyUsedException ex) {
        return Map.of("error", "ACCOUNT_CONFLICT", "message", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneric(Exception ex) {
        return Map.of("error", "INTERNAL_ERROR", "message", "Unexpected error");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleInvalidCreds(InvalidCredentialsException ex) {
        // ex.getField() 在你的类里默认是 "global"
        return Map.of(
                "error", "UNAUTHORIZED",
                "field", ex.getField(),
                "message", ex.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        // 把你 service 里的错误码原样返回给前端：CURRENT_PASSWORD_INCORRECT / NEW_PASSWORD_MISMATCH / ...
        return ResponseEntity.badRequest().body(Map.of(
                "error", "BAD_REQUEST",
                "message", ex.getMessage()
        ));
    }
    
}
