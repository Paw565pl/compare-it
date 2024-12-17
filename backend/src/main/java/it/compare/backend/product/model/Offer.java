package it.compare.backend.product.model;

import com.mongodb.lang.NonNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class Offer {
    @Field("shop")
    @NonNull private Shop shop;

    @Field("shopLogoUrl")
    @NonNull private String shopLogoUrl;

    @Indexed(unique = true)
    @Field("url")
    @NonNull private String url;

    @Field("priceHistory")
    @NonNull private List<PriceStamp> priceHistory = new ArrayList<>();
}
