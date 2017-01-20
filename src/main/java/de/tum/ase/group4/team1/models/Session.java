package de.tum.ase.group4.team1.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import de.tum.ase.group4.team1.services.AATUserService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    @JsonView(Lecture.Default.class)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @JsonView(Lecture.Default.class)
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public Key<Lecture> getLecture() { return lecture; }
    public void setLecture(Key<Lecture> lecture) { this.lecture = lecture; }

    @JsonView(Lecture.Default.class)
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @JsonView(Lecture.Default.class)
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        Lecture lecture = ObjectifyService.ofy().load().key(this.lecture).now();
        return title + (active ? " (active)" : "") + " - " + lecture.toString();
    }

    // -- Serialization --

    @JsonView(Lecture.Default.class)
    @JsonProperty("attendance")
    Map<String, Object> attendanceStatus() {
        Map<String, Object> attendanceStatus = new HashMap<>();
        UserService userService = UserServiceFactory.getUserService();
        if(userService.isUserLoggedIn()) {
            User user = userService.getCurrentUser();
            AATUser aatUser = AATUserService.getOrCreateAATUser(user);
            Attendance attendance = ObjectifyService.ofy().load().type(Attendance.class)
                    .filter("user", Key.create(aatUser)).filter("lecture", lecture).filter("session", Key.create(this))
                    .first().now();
            if(attendance != null) {
                if (attendance.verifiedAt == null) {
                    attendanceStatus.put("status", "pending");
                    attendanceStatus.put("verificationToken", attendance.verificationToken);
                } else {
                    attendanceStatus.put("status", "verified");
                }
            }
        }
        return attendanceStatus;
    }
}
