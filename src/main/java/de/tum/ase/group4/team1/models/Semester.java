package de.tum.ase.group4.team1.models;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
public class Semester {
    public @Id String slug;
    public String title;
    public @Index Date begin;
    public @Index Date end;

    public Semester() {
        Date now = new Date();
        initAroundDate(now);
    }

    public Semester(Date date) {
        initAroundDate(date);
    }

    public void initAroundDate(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        if (currentMonth < 4) {
            slug = "ws" + ((currentYear - 1) % 100) + "-" + (currentYear % 100);
            title = "WS " + ((currentYear - 1) % 100) + "/" + (currentYear % 100);
            calendar.set(currentYear-1, 10, 1);
            begin = calendar.getTime();
            calendar.set(currentYear, 3, 31);
            end = calendar.getTime();
        } else if (currentMonth > 9) {
            slug = "ws" + (currentYear % 100) + "-" + ((currentYear + 1) % 100);
            title = "WS " + (currentYear % 100) + "/" + ((currentYear + 1) % 100);
            calendar.set(currentYear, 10, 1);
            begin = calendar.getTime();
            calendar.set(currentYear + 1, 3, 31);
            end = calendar.getTime();
        } else {
            slug = "ss" + (currentYear % 100);
            title = "SS " + (currentYear % 100);
            calendar.set(currentYear, 4, 1);
            begin = calendar.getTime();
            calendar.set(currentYear, 9, 30);
            end = calendar.getTime();
        }
    }

    public Semester(String slug, String title, Date begin, Date end) {
        this.slug = slug;
        this.title = title;
        this.begin = begin;
        this.end = end;
    }

    static public Semester getCurrentSemester() {
        Date now = new Date();
        Semester currentSemester = null;
        // Filtering in-memory as GAE Datastore only supports one inequality filter per query
        List<Semester> semesters = ObjectifyService.ofy().load().type(Semester.class).list();
        for(Semester semester: semesters) {
            if(semester.begin != null && semester.end != null) {
                if (semester.begin.before(now) && semester.end.after(now)) {
                    currentSemester = semester;
                }
            }
        }
        if(currentSemester == null){
            currentSemester = new Semester();
            ObjectifyService.ofy().save().entity(currentSemester).now();
        }
        return currentSemester;
    }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Date getBegin() { return begin; }

    public void setBegin(Date begin) { this.begin = begin; }

    public Date getEnd() { return end; }

    public void setEnd(Date end) { this.end = end; }
}
