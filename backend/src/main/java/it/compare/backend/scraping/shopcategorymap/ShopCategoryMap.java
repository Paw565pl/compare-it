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
                        Category.GRAPHICS_CARD,
                        "karty-graficzne",
                        Category.PROCESSOR,
                        "procesory",
                        Category.MOTHERBOARD,
                        "plyty-glowne",
                        Category.RAM_MEMORY,
                        "pamieci-ram",
                        Category.SSD_DRIVE,
                        "dyski-wewnetrzne-ssd",
                        Category.POWER_SUPPLY,
                        "zasilacze-do-komputerow-pc",
                        Category.PC_CASE,
                        "obudowy-pc"));

        values.put(
                Shop.MORELE_NET,
                Map.of(
                        Category.GRAPHICS_CARD,
                        "/kategoria/karty-graficzne-12",
                        Category.PROCESSOR,
                        "/kategoria/procesory-45",
                        Category.MOTHERBOARD,
                        "/kategoria/plyty-glowne-42",
                        Category.RAM_MEMORY,
                        "/kategoria/pamieci-ram-38",
                        Category.SSD_DRIVE,
                        "/kategoria/dyski-ssd-518",
                        Category.POWER_SUPPLY,
                        "/kategoria/zasilacze-komputerowe-61",
                        Category.PC_CASE,
                        "/kategoria/obudowy-33"));

        values.put(
                Shop.MEDIA_EXPERT,
                Map.of(
                        Category.GRAPHICS_CARD,
                        "/komputery-i-tablety/podzespoly-komputerowe/karty-graficzne",
                        Category.PROCESSOR,
                        "/komputery-i-tablety/podzespoly-komputerowe/procesory",
                        Category.MOTHERBOARD,
                        "/komputery-i-tablety/podzespoly-komputerowe/plyty-glowne",
                        Category.RAM_MEMORY,
                        "/komputery-i-tablety/podzespoly-komputerowe/pamieci-ram",
                        Category.SSD_DRIVE,
                        "/komputery-i-tablety/dyski-i-pamieci/dyski-ssd",
                        Category.POWER_SUPPLY,
                        "/komputery-i-tablety/podzespoly-komputerowe/zasilacze",
                        Category.PC_CASE,
                        "/komputery-i-tablety/podzespoly-komputerowe/obudowy"));
    }
}
