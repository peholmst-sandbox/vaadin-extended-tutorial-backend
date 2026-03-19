package org.vaadin.tutorial.backend.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.vaadin.tutorial.backend.financial.Money;
import org.vaadin.tutorial.backend.validation.ValidationGroups.OnSave;

import java.time.LocalDate;

/**
 * Represents the full details of a product in the catalog.
 * <p>
 * This is a mutable bean that supports two states:
 * <ul>
 *   <li><b>Draft state:</b> When created via the no-arg constructor, all fields are null.
 *       This state is used during form editing.</li>
 *   <li><b>Saved state:</b> After being saved via {@link ProductCatalogService#save},
 *       required fields are guaranteed to be non-null, and {@code productId} and
 *       {@code version} are assigned by the service.</li>
 * </ul>
 * <p>
 * Required fields (validated before save): {@code name}, {@code description},
 * {@code category}, {@code brand}, {@code sku}, {@code price}.
 * <p>
 * Optional fields: {@code releaseDate}, {@code discount}.
 */
public class ProductDetails {

    private @Nullable ProductId productId;
    private @Nullable Long version;

    @NotBlank(groups = OnSave.class, message = "Name is required")
    private @Nullable String name;

    @NotBlank(groups = OnSave.class, message = "Description is required")
    private @Nullable String description;

    @NotNull(groups = OnSave.class, message = "Category is required")
    private @Nullable ProductCategoryId category;

    @NotBlank(groups = OnSave.class, message = "Brand is required")
    private @Nullable String brand;

    @NotBlank(groups = OnSave.class, message = "SKU is required")
    private @Nullable String sku;

    private @Nullable LocalDate releaseDate;

    @NotNull(groups = OnSave.class, message = "Price is required")
    private @Nullable Money price;

    private @Nullable Money discount;

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

    public @Nullable String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public @Nullable ProductCategoryId getCategory() {
        return category;
    }

    public void setCategory(@Nullable ProductCategoryId category) {
        this.category = category;
    }

    public @Nullable String getBrand() {
        return brand;
    }

    public void setBrand(@Nullable String brand) {
        this.brand = brand;
    }

    public @Nullable String getSku() {
        return sku;
    }

    public void setSku(@Nullable String sku) {
        this.sku = sku;
    }

    public @Nullable LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(@Nullable LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public @Nullable Money getPrice() {
        return price;
    }

    public void setPrice(@Nullable Money price) {
        this.price = price;
    }

    public @Nullable Money getDiscount() {
        return discount;
    }

    public void setDiscount(@Nullable Money discount) {
        this.discount = discount;
    }

}
