package com.example.motostore.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "motos")
public class Moto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;

    private String model;

    private Integer year;

    // cilindrada en cc
    private Integer engineCc;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    private Boolean active = true;

    private String description;

    private String imageKey;
    
    // ===== CONSTRUCTORES (opcional) =====

    public Moto() {
    }

    // puedes dejar solo el vacío, o agregar otro con argumentos si lo necesitas

    // ===== GETTERS Y SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getEngineCc() {
        return engineCc;
    }

    public void setEngineCc(Integer engineCc) {
        this.engineCc = engineCc;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
        public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

        @Transient
    public String getMainImageUrl() {
        if (imageKey == null || imageKey.isBlank()) {
            return "/img/motos/default-1.jpg";
        }
        return "/img/motos/" + imageKey + "-1.jpg";
    }

    // ===== PROPIEDADES DERIVADAS PARA THYMELEAF =====

    // Esto permite usar moto.name en las plantillas
    @Transient
    public String getName() {
        return brand + " " + model;
    }

    // Esto permite usar moto.engineSize en las plantillas
    @Transient
    public Integer getEngineSize() {
        return engineCc;
    }
}
