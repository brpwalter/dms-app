package com.bw.dms.controller;

import com.bw.dms.model.Category;
import com.bw.dms.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    // 1. Alle Kategorien abrufen (wird von React beim Laden aufgerufen)
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

   
    // 2. Eine neue Kategorie erstellen
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Kategoriename darf nicht leer sein!"));
        }

        Category category = new Category();
        category.setName(name);
        Category savedCategory = categoryRepository.save(category);

        return ResponseEntity.ok(savedCategory);
    }

@RequestMapping(value = "/api/categories", method = RequestMethod.POST)
@ResponseBody
public Category createCategory(@RequestBody Category category) {
    // Speichert die Kategorie in der Datenbank (die ID wird dabei generiert)
    Category savedCategory = categoryRepository.save(category); 
    
    // Gibt das Objekt mit ID direkt als JSON an React zurück
    return savedCategory; 
}


}
