package com.example.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.dto.UsuarioDTO;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@Service
public class UsuarioService {

    public static final String RUTA_USUARIOS = "LoginPrueba/Usuario";

    private final DatabaseReference usuariosRef;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioService(FirebaseDatabase firebaseDatabase) {
        this.usuariosRef = firebaseDatabase.getReference(RUTA_USUARIOS);
    }

    public void registrar(UsuarioDTO usuario) throws InterruptedException, ExecutionException, TimeoutException {
        validarTelefonoYClave(usuario.getTelefono(), usuario.getClave());

        String telefonoNormalizado = normalizarTelefono(usuario.getTelefono());
        if (existeUsuario(telefonoNormalizado)) {
            throw new IllegalStateException("Ya existe un usuario con este teléfono.");
        }

        usuario.setTelefono(telefonoNormalizado);
        usuario.setClaveCifrada(passwordEncoder.encode(usuario.getClave()));

        Map<String, Object> datos = new HashMap<>();
        datos.put("telefono", usuario.getTelefono());
        datos.put("clave", usuario.getClave());
        datos.put("claveCifrada", usuario.getClaveCifrada());

        usuariosRef.child(claveFirebase(telefonoNormalizado)).setValueAsync(datos).get(15, TimeUnit.SECONDS);
    }

    public UsuarioDTO autenticar(String telefono, String clave)
            throws InterruptedException, ExecutionException, TimeoutException {
        validarTelefonoYClave(telefono, clave);

        String telefonoNormalizado = normalizarTelefono(telefono);
        DataSnapshot snapshot = leerSnapshot(usuariosRef.child(claveFirebase(telefonoNormalizado)));

        if (!snapshot.exists()) {
            throw new IllegalArgumentException("Teléfono o clave incorrectos.");
        }

        String claveCifrada = snapshot.child("claveCifrada").getValue(String.class);
        if (claveCifrada == null || !passwordEncoder.matches(clave, claveCifrada)) {
            throw new IllegalArgumentException("Teléfono o clave incorrectos.");
        }

        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setTelefono(snapshot.child("telefono").getValue(String.class));
        usuario.setClaveCifrada(claveCifrada);
        return usuario;
    }

    private boolean existeUsuario(String telefonoNormalizado)
            throws InterruptedException, ExecutionException, TimeoutException {
        DataSnapshot snapshot = leerSnapshot(usuariosRef.child(claveFirebase(telefonoNormalizado)));
        return snapshot.exists();
    }

    private void validarTelefonoYClave(String telefono, String clave) {
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        }
        if (clave == null || clave.length() < 6) {
            throw new IllegalArgumentException("La clave debe tener al menos 6 caracteres.");
        }
    }

    private String normalizarTelefono(String telefono) {
        return telefono.replaceAll("\\s+", "").trim();
    }

    private String claveFirebase(String telefono) {
        return telefono.replace(".", "_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_")
                .replace("/", "_");
    }

    private DataSnapshot leerSnapshot(DatabaseReference ref)
            throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<DataSnapshot> future = new CompletableFuture<>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                future.complete(snapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        return future.get(15, TimeUnit.SECONDS);
    }
}
