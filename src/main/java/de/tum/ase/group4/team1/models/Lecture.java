package de.tum.ase.group4.team1.models;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public Key<Semester> getSemester() { return semester; }
    public void setSemester(Key<Semester> semester) { this.semester = semester; }

    @Override
    public String toString() {
        Semester semester = ObjectifyService.ofy().load().key(this.semester).now();
        return title + " - " + semester.toString();
    }
}
