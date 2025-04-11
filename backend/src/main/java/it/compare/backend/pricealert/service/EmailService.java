package it.compare.backend.pricealert.service;

import it.compare.backend.core.properties.CorsProperties;
import jakarta.mail.MessagingException;
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
    private final CorsProperties corsProperties;

    public void sendPriceAlert(
            String recipientEmail,
            String productName,
            String productId,
            BigDecimal currentPrice,
            BigDecimal targetPrice,
            String shopName,
            String offerUrl) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");

            var context = new Context();
            context.setVariable("productName", productName);
            context.setVariable("productId", productId);
            context.setVariable("currentPrice", currentPrice);
            context.setVariable("targetPrice", targetPrice);
            context.setVariable("shopName", shopName);
            context.setVariable("url", offerUrl);
            context.setVariable(
                    "frontendUrl", corsProperties.getAllowedOrigins().getFirst());

            var htmlContent = templateEngine.process("price-alert", context);

            helper.setTo(recipientEmail);
            helper.setSubject("OKAZJA: " + productName + " osiągnął cenę docelową!");
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@compare-it.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }
}
