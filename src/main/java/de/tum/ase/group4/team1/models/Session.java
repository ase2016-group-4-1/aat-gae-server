package de.tum.ase.group4.team1.models;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

@Entity
public class Session {
    @Id String slug;
    @Index String title;
    @Load @Parent Key<Lecture> lectureKey;
}
