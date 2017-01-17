package de.tum.ase.group4.team1.models;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

@Entity
public class Enrollment {
    @Id Long id;
    @Load @Parent Key<ExerciseGroup> exerciseGroupKey;
    @Index Key<User> userKey;
}
