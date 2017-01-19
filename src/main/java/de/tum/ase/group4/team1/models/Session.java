package de.tum.ase.group4.team1.models;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

@Entity
public class Session {
    @Id String slug;
    String title;
    @Load @Parent Key<Lecture> lecture;
    @Index Date createdAt;
    @Index boolean active = false;

    public Session() { }

    public void generateSlug() throws UnsupportedEncodingException {
        if(slug == null && title != null) {
            this.slug = URLEncoder.encode(title, "UTF-8");
        }
    }

    @OnSave private void createdNow() { this.createdAt = new Date(); }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public Key<Lecture> getLecture() { return lecture; }
    public void setLecture(Key<Lecture> lecture) { this.lecture = lecture; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        Lecture lecture = ObjectifyService.ofy().load().key(this.lecture).now();
        return title + (active ? " (active)" : "") + " - " + lecture.toString();
    }
}
