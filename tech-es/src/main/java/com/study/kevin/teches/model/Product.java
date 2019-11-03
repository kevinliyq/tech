package com.study.kevin.teches.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Objects;

public class Product {
    @JsonProperty("_id")
    private Integer productId;

    @JsonProperty("productCode")
    private String productCode;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("updatedDateTime")
    private Date updateDateTime;

    public Integer getProductId() {
        return productId;
    }

    public Product productId(Integer productId) {
        this.productId = productId;
        return this;
    }

    public String getProductCode() {
        return productCode;
    }

    public Product productCode(String productCode) {
        this.productCode = productCode;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Product title(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Product setDescription(String description) {
        this.description = description;
        return this;
    }

    public Date getUpdateDateTime() {
        return updateDateTime;
    }

    public Product setUpdateDateTime(Date updateDateTime) {
        this.updateDateTime = updateDateTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return productId.equals(product.productId) &&
                productCode.equals(product.productCode) &&
                Objects.equals(title, product.title) &&
                Objects.equals(description, product.description) &&
                Objects.equals(updateDateTime, product.updateDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productCode, title, description, updateDateTime);
    }
}
