package de.tum.ase.group4.team1.models;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;

@Entity
public class Attendance {
    @Id Long id;
    @Load @Parent Key<Session> sessionKey;
    Key<User> userKey;
    Key<ExerciseGroup> exerciseGroupKey;
    boolean verified = false;
    String token;
}
