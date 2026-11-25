package com.wim100.backend.security;

import com.wim100.backend.model.Rol;
import com.wim100.backend.model.Usuario;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String nombreCompleto;
    private final String passwordHash;
    private final Rol rol;
    private final boolean activo;

    public UsuarioPrincipal(Usuario u) {
        this.id = u.getId();
        this.username = u.getUsername();
        this.nombreCompleto = u.getNombreCompleto();
        this.passwordHash = u.getPasswordHash();
        this.rol = u.getRol();
        this.activo = u.isActivo();
    }

    public Long getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public Rol getRol() { return rol; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rol.authority()));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return activo; }
}
