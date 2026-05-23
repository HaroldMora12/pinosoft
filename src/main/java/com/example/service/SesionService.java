package com.example.service;

import org.springframework.stereotype.Service;

import com.example.dto.UsuarioDTO;
import com.vaadin.flow.server.VaadinSession;

@Service
public class SesionService {

    private static final String SESSION_USER = "usuario.sesion";

    public void iniciarSesion(UsuarioDTO usuario) {
        VaadinSession.getCurrent().setAttribute(SESSION_USER, usuario);
    }

    public void cerrarSesion() {
        VaadinSession.getCurrent().setAttribute(SESSION_USER, null);
    }

    public boolean haySesionActiva() {
        return obtenerUsuario() != null;
    }

    public UsuarioDTO obtenerUsuario() {
        return (UsuarioDTO) VaadinSession.getCurrent().getAttribute(SESSION_USER);
    }
}
