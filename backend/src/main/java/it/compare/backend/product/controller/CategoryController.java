package it.compare.backend.product.controller;

import it.compare.backend.product.model.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    @GetMapping
    public Category[] findAll() {
        return Category.values();
    }
}
