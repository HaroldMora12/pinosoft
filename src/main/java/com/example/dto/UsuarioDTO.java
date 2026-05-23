package com.example.dto;

/**
 * Representa un usuario con teléfono, clave en texto plano (formulario) y clave cifrada (persistencia).
 */
public class UsuarioDTO {

    private String telefono;
    private String clave;
    private String claveCifrada;

    public UsuarioDTO() {
    }

    public UsuarioDTO(String telefono, String clave, String claveCifrada) {
        this.telefono = telefono;
        this.clave = clave;
        this.claveCifrada = claveCifrada;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getClaveCifrada() {
        return claveCifrada;
    }

    public void setClaveCifrada(String claveCifrada) {
        this.claveCifrada = claveCifrada;
    }
}
