package de.tum.ase.group4.team1.models;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

@Entity
public class Semester {
    public @Id String title;
    public @Index Date begin;
    public @Index Date end;

    public Semester() { }

    public Semester(String title, Date begin, Date end) {
        this.title = title;
        this.begin = begin;
        this.end = end;
    }
}
