package it.compare.backend.product.controller;

import it.compare.backend.product.model.Category;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    @GetMapping
    public List<String> getAllCategories() {
        return Arrays.stream(Category.values())
                .map(Category::getHumanReadableName)
                .toList();
    }
}
