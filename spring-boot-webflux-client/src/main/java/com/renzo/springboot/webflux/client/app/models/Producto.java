package com.renzo.springboot.webflux.client.app.models;

import java.util.Date;

public class Producto {
	private String id;
	private String nombre;
	private Double precio;
	private Date createAt;
	private Categoria categoria;
	
	public Producto(String nombre, Double precio, Date createAt, Categoria categoria) {
		this.nombre = nombre;
		this.precio = precio;
		this.createAt = createAt;
		this.categoria = categoria;
	}
	
	public Producto() {
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Double getPrecio() {
		return precio;
	}
	public void setPrecio(Double precio) {
		this.precio = precio;
	}
	public Date getCreateAt() {
		return createAt;
	}
	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}
	public Categoria getCategoria() {
		return categoria;
	}
	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}
}
