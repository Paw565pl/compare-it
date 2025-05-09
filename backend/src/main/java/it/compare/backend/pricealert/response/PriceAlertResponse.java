package it.compare.backend.pricealert.response;

import com.mongodb.lang.Nullable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PriceAlertResponse {
    private String id;
    private String productId;
    private String productName;
    private BigDecimal targetPrice;
    private BigDecimal currentLowestPrice;
    private Boolean isOutletAllowed;
    private Boolean isActive;
    private LocalDateTime createdAt;

    @Nullable private LocalDateTime lastNotificationSent;
}
