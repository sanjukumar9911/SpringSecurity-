package com.esrx.services.benefit.group.onboarding.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@JsonAutoDetect
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "Product")
@ApiModel(value = "ProductEntity")
public class ProductEntity {

	@Id
	@GeneratedValue
	@ApiModelProperty(name = "ID", notes = "Key notes, No need to send by Client")
	Long id;
	
	@Column(name = "ProductName", length = 50)
	@ApiModelProperty(name = "ProductName", notes = "Product name" )
	@Size(max = 10, message = "product name Size is invalid")
	String productName;
	
	@Column(name = "ProductType", length = 50)
	@ApiModelProperty(name = "ProductType", notes = "Product Type" )
	@Size(max = 10, message = "product Type Size is invalid")
	String productType;
	
	@Column(name = "ProductPrice", length = 50)
	@ApiModelProperty(name = "ProductPrice", notes = "Product Price" )
	@Size(max = 10, message = "product Price Size is invalid")
	String productPrice;
	
	@Column(name = "ProductOwner", length = 50)
	@ApiModelProperty(name = "ProductOwner", notes = "Product Owner" )
	@Size(max = 10, message = "product Owner Size is invalid")
	String productOwner;
	
	@Column(name = "ProductPlace", length = 50)
	@ApiModelProperty(name = "ProductPlace", notes = "Product Place" )
	@Size(max = 10, message = "product Place Size is invalid")
	String productPlace;

	
	public ProductEntity() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(String productPrice) {
		this.productPrice = productPrice;
	}

	public String getProductOwner() {
		return productOwner;
	}

	public void setProductOwner(String productOwner) {
		this.productOwner = productOwner;
	}

	public String getProductPlace() {
		return productPlace;
	}

	public void setProductPlace(String productPlace) {
		this.productPlace = productPlace;
	}
	
	
	
}
