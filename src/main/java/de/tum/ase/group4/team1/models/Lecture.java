package de.tum.ase.group4.team1.models;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Entity
public class Lecture {
    @Id String slug;
    String title;
    @Load @Parent Key<Semester> semesterKey;

    public Lecture() { }

    public Lecture(String title, String semester) {
        if(semester != null) {
            this.semesterKey = Key.create(Semester.class, semester);
        } else {
            this.semesterKey = Key.create(Semester.getSemesterForCurrentDate());
        }
        this.title = title;
        this.slug = getSlug();
    }

    public Lecture(String title, Semester semester) {
        if(semester != null) {
            this.semesterKey = Key.create(semester);
        } else {
            this.semesterKey = Key.create(Semester.getSemesterForCurrentDate());
        }
        this.title = title;
        this.slug = getSlug();
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() {
        if(slug == null && title != null){
            try {
                slug = URLEncoder.encode(title, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return slug;
    }
    public void setSlug(String slug) { this.slug = slug; }

    public Key<Semester> getSemesterKey() { return semesterKey; }
    public void setSemesterKey(Key<Semester> semesterKey) { this.semesterKey = semesterKey; }

    public Semester getSemester() {
        if(semesterKey != null) {
            return ObjectifyService.ofy().load().key(semesterKey).now();
        }
        return new Semester();
    }
    public void setSemester(String semesterSlug) { semesterKey = Key.create(Semester.class, semesterSlug); }

    @Override public String toString() {
        return getSemester().title + " - " + title;
    }
}
