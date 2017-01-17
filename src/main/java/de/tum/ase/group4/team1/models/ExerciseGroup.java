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
public class ExerciseGroup {
    @Id String slug;
    String title;
    @Load @Parent Key<Lecture> lectureKey;

    public ExerciseGroup() { }

    public ExerciseGroup(String title, String lectureSlug){
        this.title = title;
        this.slug = getSlug();
        if(lectureSlug != null) {
            this.lectureKey = Key.create(Lecture.class, lectureSlug);
        }
    }

    public ExerciseGroup(String title, Lecture lecture) {
        this.title = title;
        this.slug = getSlug();
        if(lecture != null){
            this.lectureKey = Key.create(lecture);
        }
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
        return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public Key<Lecture> getLectureKey() { return lectureKey; }
    public void setLectureKey(Key<Lecture> lectureKey) { this.lectureKey = lectureKey; }

    public Lecture getLecture() {
        if(lectureKey != null) {
            return ObjectifyService.ofy().load().key(lectureKey).now();
        }
        return new Lecture(); }
    public void setLecture(String lectureSlug) { lectureKey = Key.create(Lecture.class, lectureSlug); }

    @Override
    public String toString() {
        return getLecture().title + " - " + title;
    }
}
