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

    @NonNull @Indexed
    @Field("shop")
    private Shop shop;

    @NonNull @Field("url")
    private String url;

    @NonNull @Field("priceHistory")
    private List<PriceStamp> priceHistory = new ArrayList<>();
}
