package de.tum.ase.group4.team1.models;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class AATUser {
    @Id String userId;
    @Index String email;
    @Index String authDomain;

    public AATUser() { }

    public AATUser(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.authDomain = user.getAuthDomain();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAuthDomain() { return authDomain; }
    public void setAuthDomain(String authDomain) { this.authDomain = authDomain; }

    @Override
    public String toString() { return email; }

    public boolean equals(User other) {
        return email.equals(other.getEmail()) && authDomain.equals(other.getAuthDomain());
    }
}
