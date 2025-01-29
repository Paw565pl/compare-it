package it.compare.backend.pricealert.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PriceAlertResponse {
    private String id;
    private String productId;
    private String productName;
    private BigDecimal targetPrice;
    private BigDecimal currentLowestPrice;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastNotificationSent;
}
