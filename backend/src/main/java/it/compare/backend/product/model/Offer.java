package it.compare.backend.product.model;

import com.mongodb.lang.NonNull;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
public class Offer {
    @Indexed
    @Field("shop")
    @NonNull private Shop shop;

    @Indexed(unique = true)
    @Field("url")
    @NonNull private String url;

    @Field("priceHistory")
    @NonNull private List<PriceStamp> priceHistory = new ArrayList<>();
}
