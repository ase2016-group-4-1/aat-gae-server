package de.tum.ase.group4.team1.models;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

@Entity
public class Lecture {
    @Id String title;
    @Parent Key<Semester> semesterKey;

    public Lecture() { }

    public Lecture(String title, String semester) {

    }
}
