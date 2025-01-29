package it.compare.backend.pricealert.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertResponse {
    private String id;
    private String productId;
    private String productName;
    private BigDecimal targetPrice;
    private BigDecimal currentLowestPrice;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastNotificationSent;
}
