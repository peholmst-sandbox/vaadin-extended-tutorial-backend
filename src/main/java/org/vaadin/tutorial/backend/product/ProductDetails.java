package org.vaadin.tutorial.backend.product;

import org.jspecify.annotations.Nullable;
import org.vaadin.tutorial.backend.financial.Money;

import java.time.LocalDate;

public class ProductDetails {

    private @Nullable ProductId productId;
    private @Nullable Long version;
    private String name;
    private String description;
    private String category;
    private String brand;
    private String sku;
    private LocalDate releaseDate;
    private Money price;
    private Money discount;

    public ProductDetails() {
    }

    public ProductDetails(ProductDetails original) {
        this.productId = original.productId;
        this.version = original.version;
        this.name = original.name;
        this.description = original.description;
        this.category = original.category;
        this.brand = original.brand;
        this.sku = original.sku;
        this.releaseDate = original.releaseDate;
        this.price = original.price;
        this.discount = original.discount;
    }

    public @Nullable ProductId getProductId() {
        return productId;
    }

    void setProductId(ProductId productId) {
        this.productId = productId;
    }

    public @Nullable Long getVersion() {
        return version;
    }

    void setVersion(Long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Money getPrice() {
        return price;
    }

    public void setPrice(Money price) {
        this.price = price;
    }

    public Money getDiscount() {
        return discount;
    }

    public void setDiscount(Money discount) {
        this.discount = discount;
    }

}
