package com.bw.dms.model;

import jakarta.persistence.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password; // Wird als bcrypt-Hash gespeichert!

    // Getter und Setter
    public Long getId() { return id; }
    public String getUsername() { return username; }
        public void setUsername(String username) { 
    this.username = username; 
}
    public String getPassword() { return password; }
    public void setPassword(String password) { 
    this.password = password; 
}

    // Spring Security Anforderungen (Methoden von UserDetails)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // Hier könnten Rollen wie "ROLE_USER" stehen
    }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    public String getEmail() { return email; }
public void setEmail(String email) { this.email = email; }
}


