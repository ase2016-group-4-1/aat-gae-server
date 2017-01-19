package de.tum.ase.group4.team1.models;

import com.fasterxml.jackson.annotation.JsonView;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Entity
public class ExerciseGroup {
    @Id String slug;
    String title;
    @Load @Parent Key<Lecture> lecture;

    public ExerciseGroup() { }

    public void generateSlug() throws UnsupportedEncodingException {
        if(slug == null && title != null) {
            this.slug = URLEncoder.encode(title, "UTF-8");
        }
    }

    @JsonView(Lecture.Default.class)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @JsonView(Lecture.Default.class)
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public Key<Lecture> getLecture() { return lecture; }
    public void setLecture(Key<Lecture> lecture) { this.lecture = lecture; }

    @Override
    public String toString() {
        Lecture lecture = ObjectifyService.ofy().load().key(this.lecture).now();
        return title + " - " + lecture.toString();
    }
}
