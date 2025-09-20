package it.compare.backend.product.model;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
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

        var latestAvailableOffers = product.getOffers().stream()
                .map(offer -> offer.getPriceHistory().stream()
                        .max(Comparator.comparing(PriceStamp::getTimestamp))
                        .map(latestPrice -> new OfferWithLatestPrice(offer, latestPrice)))
                .flatMap(Optional::stream)
                .filter(o -> o.latestPriceStamp().getTimestamp().isAfter(cutOff))
                .toList();
        var availableOffersSize = latestAvailableOffers.size();

        var lowestOffer = latestAvailableOffers.stream()
                .min(Comparator.comparing(o -> o.latestPriceStamp().getPrice()))
                .orElse(null);
        if (lowestOffer == null) return computedState;

        computedState.setAvailableOffersCount(availableOffersSize);
        computedState.setBestOffer(new BestOffer(
                lowestOffer.offer().getShop(),
                lowestOffer.offer().getUrl(),
                lowestOffer.latestPriceStamp().getPrice(),
                lowestOffer.latestPriceStamp().getCurrency(),
                lowestOffer.latestPriceStamp().getCondition()));

        return computedState;
    }

    private record OfferWithLatestPrice(Offer offer, PriceStamp latestPriceStamp) {}
}
