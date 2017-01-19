package de.tum.ase.group4.team1.models;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Calendar;
import java.util.Date;

@Entity
public class Semester {
    @Id String slug;
    String title;
    @Index Date begin;
    @Index Date end;

    public Semester() {
        init(new Date());
    }

    public Semester(Date date) {
        init(date);
    }

    public Semester(String slug, String title, Date begin, Date end) {
        this.slug = slug;
        this.title = title;
        this.begin = begin;
        this.end = end;
    }

    private void init(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        Calendar beginCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        if (currentMonth < 4) {
            title = "WS " + ((currentYear - 1) % 100) + "/" + (currentYear % 100);
            beginCalendar.set(currentYear-1, 10, 1);
            endCalendar.set(currentYear, 3, 31);
        } else if (currentMonth > 9) {
            title = "WS " + (currentYear % 100) + "/" + ((currentYear + 1) % 100);
            beginCalendar.set(currentYear, 10, 1);
            endCalendar.set(currentYear + 1, 3, 31);
        } else {
            title = "SS " + (currentYear % 100);
            beginCalendar.set(currentYear, 4, 1);
            endCalendar.set(currentYear, 9, 30);
        }
        slug = title.toLowerCase().replace("/", "-");
        begin = beginCalendar.getTime();
        end = endCalendar.getTime();
    }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Date getBegin() { return begin; }
    public void setBegin(Date begin) { this.begin = begin; }

    public Date getEnd() { return end; }
    public void setEnd(Date end) { this.end = end; }

    @Override
    public String toString() { return title; }
}
