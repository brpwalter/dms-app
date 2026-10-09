package com.bw.dms.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "categories") // oder "category", je nach deinem Benennungsschema
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <-- WICHTIG FÜR POSTGRESQL!
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
