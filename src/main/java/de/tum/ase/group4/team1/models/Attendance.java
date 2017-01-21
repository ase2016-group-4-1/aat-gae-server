package de.tum.ase.group4.team1.models;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnSave;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

@Entity
public class Attendance {
    @Id Long id;
    @Index Key<AATUser> user;
    @Index Key<Lecture> lecture;
    @Index Key<Session> session;
    @Index Date createdAt;
    @Index Date verifiedAt;
    @Index Key<ExerciseGroup> exerciseGroupOnCreation;
    @Index Key<ExerciseGroup> exerciseGroupOnVerification;
    @Index String verificationToken;
    @Index String mode;

    // Secure random objects are expensive to initialize, so do this once and keep it around
    static private SecureRandom random;
    static {
        random = new SecureRandom();
        random.setSeed(System.currentTimeMillis());
    }

    public Attendance() {
        // Generate verification token
        verificationToken = new BigInteger(130, random).toString(32);
    }

    @OnSave private void createdNow() { this.createdAt = new Date(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Key<AATUser> getUser() { return user; }
    public void setUser(Key<AATUser> user) { this.user = user; }

    public Key<Lecture> getLecture() { return lecture; }
    public void setLecture(Key<Lecture> lecture) { this.lecture = lecture; }

    public Key<Session> getSession() { return session; }
    public void setSession(Key<Session> session) { this.session = session; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Date verifiedAt) { this.verifiedAt = verifiedAt; }

    public Key<ExerciseGroup> getExerciseGroupOnCreation() { return exerciseGroupOnCreation; }
    public void setExerciseGroupOnCreation(Key<ExerciseGroup> exerciseGroupOnCreation) { this.exerciseGroupOnCreation = exerciseGroupOnCreation; }

    public Key<ExerciseGroup> getExerciseGroupOnVerification() { return exerciseGroupOnVerification; }
    public void setExerciseGroupOnVerification(Key<ExerciseGroup> exerciseGroupOnVerification) { this.exerciseGroupOnVerification = exerciseGroupOnVerification; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
