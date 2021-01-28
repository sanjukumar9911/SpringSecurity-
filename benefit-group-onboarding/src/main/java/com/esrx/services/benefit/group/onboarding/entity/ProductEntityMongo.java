/*
 * package com.esi.entity.demo.demo.entity;
 * 
 * import javax.persistence.Column; import javax.persistence.Entity; import
 * javax.persistence.GeneratedValue; import javax.persistence.Id; import
 * javax.persistence.Table;
 * 
 * import io.swagger.annotations.ApiModel; import
 * io.swagger.annotations.ApiModelProperty;
 * 
 * @Entity
 * 
 * @Table(name = "Product")
 * 
 * @ApiModel(value = "ProductEntity") public class ProductEntityMongo {
 * 
 * @Id
 * 
 * @GeneratedValue
 * 
 * @ApiModelProperty(name = "ID", value =
 * "Key value, No need to send by Client") Long id;
 * 
 * @Column(name = "ProductName", length = 50)
 * 
 * @ApiModelProperty(name = "ProductName", value = "Product name" ) String
 * productName;
 * 
 * @Column(name = "ProductType", length = 50)
 * 
 * @ApiModelProperty(name = "ProductType", value = "Product Type" ) String
 * productType;
 * 
 * @Column(name = "ProductPrice", length = 50)
 * 
 * @ApiModelProperty(name = "ProductPrice", value = "Product Price" ) String
 * productPrice;
 * 
 * @Column(name = "ProductOwner", length = 50)
 * 
 * @ApiModelProperty(name = "ProductOwner", value = "Product Owner" ) String
 * productOwner;
 * 
 * @Column(name = "ProductPlace", length = 50)
 * 
 * @ApiModelProperty(name = "ProductPlace", value = "Product Place" ) String
 * productPlace;
 * 
 * 
 * public ProductEntityMongo() { super(); }
 * 
 * public Long getId() { return id; }
 * 
 * public void setId(Long id) { this.id = id; }
 * 
 * public String getProductName() { return productName; }
 * 
 * public void setProductName(String productName) { this.productName =
 * productName; }
 * 
 * public String getProductType() { return productType; }
 * 
 * public void setProductType(String productType) { this.productType =
 * productType; }
 * 
 * public String getProductPrice() { return productPrice; }
 * 
 * public void setProductPrice(String productPrice) { this.productPrice =
 * productPrice; }
 * 
 * public String getProductOwner() { return productOwner; }
 * 
 * public void setProductOwner(String productOwner) { this.productOwner =
 * productOwner; }
 * 
 * public String getProductPlace() { return productPlace; }
 * 
 * public void setProductPlace(String productPlace) { this.productPlace =
 * productPlace; }
 * 
 * 
 * 
 * }
 */