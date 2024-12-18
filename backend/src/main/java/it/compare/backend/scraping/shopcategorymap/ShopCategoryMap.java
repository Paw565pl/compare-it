package it.compare.backend.scraping.shopcategorymap;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Shop;
import java.util.EnumMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ShopCategoryMap {

    private final Map<Shop, Map<Category, String>> values = new EnumMap<>(Shop.class);

    private ShopCategoryMap() {
        values.put(
                Shop.RTV_EURO_AGD,
                Map.of(
                        Category.GRAPHICS_CARDS,
                        "karty-graficzne",
                        Category.PROCESSORS,
                        "procesory",
                        Category.MOTHERBOARDS,
                        "plyty-glowne",
                        Category.RAM_MEMORY,
                        "pamieci-ram"));

        values.put(
                Shop.MORELE_NET,
                Map.of(
                        Category.GRAPHICS_CARDS,
                        "karty-graficzne",
                        Category.PROCESSORS,
                        "procesory",
                        Category.MOTHERBOARDS,
                        "plyty-glowne",
                        Category.RAM_MEMORY,
                        "pamieci-ram"));
    }
}
