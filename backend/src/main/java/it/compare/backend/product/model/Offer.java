package it.compare.backend.product.model;

import com.mongodb.lang.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
public class Offer {

    @NonNull @Indexed
    @Field("shop")
    private Shop shop;

    @NonNull @Field("url")
    private String url;

    @NonNull @Field("latestPriceStamp")
    private PriceStamp latestPriceStamp;

    @NonNull @Field("priceHistory")
    private List<PriceStamp> priceHistory = new ArrayList<>();

    public Offer(@NonNull Shop shop, @NonNull String url, @NonNull PriceStamp priceStamp) {
        this.shop = shop;
        this.url = url;
        this.latestPriceStamp = priceStamp;
        priceHistory.add(priceStamp);
    }

    @NonNull public List<PriceStamp> getPriceHistory() {
        return Collections.unmodifiableList(priceHistory);
    }

    public void addPriceStamp(@NonNull PriceStamp priceStamp) {
        if (!priceStamp.getTimestamp().equals(latestPriceStamp.getTimestamp())) priceHistory.add(priceStamp);

        if (priceStamp.getTimestamp().isAfter(latestPriceStamp.getTimestamp())) setLatestPriceStamp(priceStamp);
    }
}
