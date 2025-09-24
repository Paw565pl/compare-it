package it.compare.backend.product.model;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
public class ComputedState {

    @Transient
    public static final Duration AVAILABILITY_DAYS_THRESHOLD = Duration.ofDays(3);

    @Nullable @Field("bestOffer")
    private BestOffer bestOffer;

    @NonNull @Indexed
    @Field("availableOffersCount")
    private Integer availableOffersCount = 0;

    public static ComputedState fromProduct(Product product) {
        var computedState = new ComputedState();
        var cutOff = Instant.now().minus(AVAILABILITY_DAYS_THRESHOLD);

        var availableOffers = product.getOffers().stream()
                .filter(o -> o.getLatestPriceStamp().getTimestamp().isAfter(cutOff))
                .toList();
        var availableOffersSize = availableOffers.size();

        var lowestOffer = availableOffers.stream()
                .min(Comparator.comparing(o -> o.getLatestPriceStamp().getPrice()))
                .orElse(null);
        if (lowestOffer == null) return computedState;

        computedState.setAvailableOffersCount(availableOffersSize);
        computedState.setBestOffer(new BestOffer(
                lowestOffer.getShop(),
                lowestOffer.getUrl(),
                lowestOffer.getLatestPriceStamp().getPrice(),
                lowestOffer.getLatestPriceStamp().getCurrency(),
                lowestOffer.getLatestPriceStamp().getCondition()));

        return computedState;
    }
}
