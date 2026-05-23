package com.example.views;

import com.example.dto.UsuarioDTO;
import com.example.service.SesionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("inicio")
@PageTitle("Inicio | Pinosoft")
public class InicioView extends VerticalLayout implements BeforeEnterObserver {

    private final SesionService sesionService;

    public InicioView(SesionService sesionService) {
        this.sesionService = sesionService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("inicio-view");

        H2 bienvenida = new H2("Bienvenido");
        bienvenida.addClassName("inicio-titulo");

        Paragraph telefono = new Paragraph();
        telefono.addClassName("inicio-telefono");

        Button cerrarSesion = new Button("Cerrar sesión", e -> {
            sesionService.cerrarSesion();
            getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        });
        cerrarSesion.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        add(bienvenida, telefono, cerrarSesion);

        UsuarioDTO usuario = sesionService.obtenerUsuario();
        if (usuario != null) {
            telefono.setText("Sesión activa: " + usuario.getTelefono());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sesionService.haySesionActiva()) {
            event.forwardTo(LoginView.class);
        }
    }
}
