package cl.oyl.productos.model;

import jakarta.persistence.*;

/** Entidad JPA: un producto del catálogo (tabla "products"). */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, length = 20)
    private String unit = "kg";

    @Column(length = 1000)
    private String description = "";

    @Column(length = 1000)
    private String ingredients = "";

    @Column(length = 200)
    private String origin = "Chile";

    @Column(length = 40, unique = true)
    private String barcode;

    @Column(length = 500)
    private String imageUrl = "";

    private Boolean isFavorite = false;

    public Product() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Boolean getIsFavorite() { return isFavorite; }
    public void setIsFavorite(Boolean isFavorite) { this.isFavorite = isFavorite; }
}
