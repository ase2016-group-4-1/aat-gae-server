package de.tum.ase.group4.team1.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Lecture {
    @Id String slug;
    String title;
    @Index @Parent Key<Semester> semester;

    public Lecture() { }

    public void generateSlug() throws UnsupportedEncodingException {
        if(slug == null && title != null) {
            this.slug = URLEncoder.encode(title, "UTF-8");
        }
    }

    @JsonView(Default.class)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @JsonView(Default.class)
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public Key<Semester> getSemester() { return semester; }
    public void setSemester(Key<Semester> semester) { this.semester = semester; }

    @Override
    public String toString() {
        Semester semester = ObjectifyService.ofy().load().key(this.semester).now();
        return title + " - " + semester.toString();
    }

    // -- Serialization --

    public interface Default {};

    @JsonView(Default.class)
    @JsonProperty("semester")
    Semester loadSemester() {
        return ObjectifyService.ofy().load().key(semester).now();
    }

    @JsonView(Default.class)
    @JsonProperty("exerciseGroups")
    @Ignore
    public List<ExerciseGroup> exerciseGroups = new ArrayList<>();

    @JsonView(Default.class)
    @JsonProperty("sessions")
    @Ignore
    public List<Session> sessions = new ArrayList<>();
}
