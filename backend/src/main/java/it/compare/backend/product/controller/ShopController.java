package it.compare.backend.product.controller;

import it.compare.backend.product.model.Shop;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shops")
public class ShopController {

    @GetMapping
    public Shop[] findAll() {
        return Shop.values();
    }
}
