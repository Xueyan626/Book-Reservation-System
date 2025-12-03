package usyd.library_reservation_system.library_reservation_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:Library Reservation <lzhua002623@gmail.com>}")
    private String from;

    public void sendLoginCode(String to, String code, int ttlMinutes) {
        var subject = "Your login verification code";
        var text = """
        Your login verification code is: %s
        It will expire in %d minutes.

        If you did not attempt to log in, please ignore this email.
        """.formatted(code, ttlMinutes);
        var msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }

    public void sendRegisterCode(String to, String code, int ttlMinutes) {
        var subject = "Your registration code";
        var text = "Your code is: %s (valid for %d minutes)".formatted(code, ttlMinutes);

        var msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }

}
