package com.example.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_specs")
@Getter
@Setter
@NoArgsConstructor
public class ProductSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            unique = true
    )
    private Product product;

    @Column(length = 20)
    private String ram;

    @Column(name = "storage", length = 20)
    private String storage;

    @Column(length = 50)
    private String color;

    @Column(name = "screen_size", length = 20)
    private String screenSize;

    @Column(name = "battery", length = 20)
    private String battery;
}
