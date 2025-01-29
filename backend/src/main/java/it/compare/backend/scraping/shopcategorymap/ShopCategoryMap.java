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
                        "pamieci-ram",
                        Category.SSD_DRIVES,
                        "dyski-wewnetrzne-ssd",
                        Category.POWER_SUPPLY,
                        "zasilacze-do-komputerow-pc",
                        Category.PC_CASE,
                        "obudowy-pc"));

        values.put(
                Shop.MORELE_NET,
                Map.of(
                        Category.GRAPHICS_CARDS,
                        "/kategoria/karty-graficzne-12",
                        Category.PROCESSORS,
                        "/kategoria/procesory-45",
                        Category.MOTHERBOARDS,
                        "/kategoria/plyty-glowne-42",
                        Category.RAM_MEMORY,
                        "/kategoria/pamieci-ram-38",
                        Category.SSD_DRIVES,
                        "/kategoria/dyski-ssd-518",
                        Category.POWER_SUPPLY,
                        "/kategoria/zasilacze-komputerowe-61",
                        Category.PC_CASE,
                        "/kategoria/obudowy-33"));
    }
}
