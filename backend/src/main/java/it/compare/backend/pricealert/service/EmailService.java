package it.compare.backend.pricealert.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendPriceAlert(
            String recipientEmail,
            String productName,
            String productId,
            BigDecimal currentPrice,
            BigDecimal targetPrice,
            String shopName,
            String offerUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("productName", productName);
            context.setVariable("productId", productId);
            context.setVariable("currentPrice", currentPrice);
            context.setVariable("targetPrice", targetPrice);
            context.setVariable("shopName", shopName);
            context.setVariable("url", offerUrl);

            String htmlContent = templateEngine.process("price-alert", context);

            helper.setTo(recipientEmail);
            helper.setSubject("Price Alert: " + productName + " reached your target price!");
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@compare-it.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }
}
