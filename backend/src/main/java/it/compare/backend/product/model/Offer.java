package it.compare.backend.product.model;

import com.mongodb.lang.NonNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class Offer {
    @Field("shop")
    @NonNull private Shop shop;

    @Field("url")
    @NonNull private String url;

    @Field("priceHistory")
    @NonNull private List<PriceStamp> priceHistory;
}
