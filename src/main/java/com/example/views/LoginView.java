package com.example.views;

import com.example.dto.UsuarioDTO;
import com.example.service.SesionService;
import com.example.service.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("")
@PageTitle("Iniciar sesión | Pinosoft")
@AnonymousAllowed
public class LoginView extends Div implements BeforeEnterObserver {

    private final UsuarioService usuarioService;
    private final SesionService sesionService;

    private final TextField telefonoField = new TextField("Teléfono");
    private final PasswordField claveField = new PasswordField("Clave");
    private final Button accionButton = new Button("Iniciar sesión");

    private boolean modoRegistro = false;

    public LoginView(UsuarioService usuarioService, SesionService sesionService) {
        this.usuarioService = usuarioService;
        this.sesionService = sesionService;

        addClassName("login-view");

        Div card = new Div();
        card.addClassName("login-card");

        H1 titulo = new H1("Pinosoft");
        titulo.addClassName("login-title");
        Paragraph subtitulo = new Paragraph("Accede con tu teléfono y clave");
        subtitulo.addClassName("login-subtitle");

        Tab tabLogin = new Tab("Iniciar sesión");
        Tab tabRegistro = new Tab("Registrarse");
        Tabs tabs = new Tabs(tabLogin, tabRegistro);
        tabs.addClassName("login-tabs");
        tabs.addSelectedChangeListener(event -> {
            modoRegistro = event.getSelectedTab() == tabRegistro;
            accionButton.setText(modoRegistro ? "Crear cuenta" : "Iniciar sesión");
            subtitulo.setText(modoRegistro
                    ? "Registra tu teléfono para acceder al sistema"
                    : "Accede con tu teléfono y clave");
        });

        telefonoField.setPlaceholder("+34 600 000 000");
        telefonoField.setPrefixComponent(com.vaadin.flow.component.icon.VaadinIcon.PHONE.create());
        telefonoField.setRequired(true);
        telefonoField.setClearButtonVisible(true);
        telefonoField.setWidthFull();

        claveField.setPlaceholder("Mínimo 6 caracteres");
        claveField.setPrefixComponent(com.vaadin.flow.component.icon.VaadinIcon.LOCK.create());
        claveField.setRequired(true);
        claveField.setWidthFull();

        accionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        accionButton.setWidthFull();
        accionButton.addClickListener(e -> ejecutarAccion());

        VerticalLayout form = new VerticalLayout(
                titulo, subtitulo, tabs, telefonoField, claveField, accionButton);
        form.setPadding(false);
        form.setSpacing(true);
        form.setWidthFull();
        form.addClassName("login-form");

        card.add(form);
        add(card);
    }

    private void ejecutarAccion() {
        telefonoField.setInvalid(false);
        claveField.setInvalid(false);

        String telefono = telefonoField.getValue();
        String clave = claveField.getValue();

        if (telefono == null || telefono.isBlank()) {
            telefonoField.setInvalid(true);
            telefonoField.setErrorMessage("Introduce tu teléfono");
            return;
        }
        if (clave == null || clave.length() < 6) {
            claveField.setInvalid(true);
            claveField.setErrorMessage("La clave debe tener al menos 6 caracteres");
            return;
        }

        accionButton.setEnabled(false);

        try {
            if (modoRegistro) {
                UsuarioDTO usuario = new UsuarioDTO();
                usuario.setTelefono(telefono);
                usuario.setClave(clave);
                usuarioService.registrar(usuario);
                mostrarExito("Cuenta creada correctamente. Ya puedes iniciar sesión.");
            } else {
                UsuarioDTO usuario = usuarioService.autenticar(telefono, clave);
                sesionService.iniciarSesion(usuario);
                getUI().ifPresent(ui -> ui.navigate(InicioView.class));
            }
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        } finally {
            accionButton.setEnabled(true);
        }
    }

    private void mostrarError(String mensaje) {
        Notification notification = Notification.show(mensaje, 4000,
                Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void mostrarExito(String mensaje) {
        Notification notification = Notification.show(mensaje, 4000,
                Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (sesionService.haySesionActiva()) {
            event.forwardTo(InicioView.class);
        }
    }
}
