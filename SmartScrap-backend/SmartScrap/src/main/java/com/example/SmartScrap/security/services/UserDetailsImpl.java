package com.example.SmartScrap.security.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.SmartScrap.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
public class UserDetailsImpl implements UserDetails {
private Long id;
private String email;
@JsonIgnore
private String password;
private Collection<? extends GrantedAuthority> authorities;
public UserDetailsImpl(Long id, String email, String password, Collection<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.authorities = authorities;
}

public static UserDetailsImpl build(User user) {
    List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
    return new UserDetailsImpl(user.getId(), user.getEmail(), user.getPassword(), authorities);
}

// Getters for id and email
public Long getId() { return id; }
public String getEmail() { return email; }

@Override
public String getUsername() { return email; } // Use email as the username

@Override
public String getPassword() { return password; }

@Override
public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

// Other UserDetails methods (you can leave them as is)
@Override public boolean isAccountNonExpired() { return true; }
@Override public boolean isAccountNonLocked() { return true; }
@Override public boolean isCredentialsNonExpired() { return true; }
@Override public boolean isEnabled() { return true; }
}