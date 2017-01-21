package de.tum.ase.group4.team1.services;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.AATUser;
import de.tum.ase.group4.team1.models.Attendance;
import de.tum.ase.group4.team1.models.Enrollment;
import de.tum.ase.group4.team1.models.Lecture;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

public class NotificationService {
    public void sendNotification(Key<Lecture> lectureKey) {
        List<Enrollment> enrollments = ObjectifyService.ofy().load().type(Enrollment.class)
                .filter("lecture", lectureKey).list();
        for(Enrollment enrollment : enrollments) {
            Key<AATUser> userKey = enrollment.getUser();
            int countOfSessions = ObjectifyService.ofy().load().type(Session.class).ancestor(lectureKey).count();
            int countOfAttendances = ObjectifyService.ofy().load().type(Attendance.class)
                    .filter("user", userKey).filter("lecture", lectureKey).filter("mode", "attendance").count();
            int countOfPresentations = ObjectifyService.ofy().load().type(Attendance.class)
                    .filter("user", userKey).filter("lecture", lectureKey).filter("mode", "presentation").count();

            AATUser user = ObjectifyService.ofy().load().key(userKey).now();
            Lecture lecture = ObjectifyService.ofy().load().key(lectureKey).now();
            if((countOfAttendances + countOfPresentations >= countOfSessions - 2) && countOfPresentations > 0){
                enrollment.setBonus(true);
                ObjectifyService.ofy().save().entity(enrollment).now();
                sendEmail(user.getEmail(), lecture.getTitle(), true);
            } else {
                sendEmail(user.getEmail(), lecture.getTitle(), false);
            }
        }
    }

    private void sendEmail(String email, String lectureName, boolean success){
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("no-reply@ase2016-group-4-1.appspotmail.com", lectureName));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(email));
            msg.setSubject(lectureName + " - Bonus notification");

            if(success) {
                msg.setText("Hey everybody, \n\n" +
                        "This is to notify you that you have been awarded with the bonus for the " + lectureName + " course." +
                        "Congratulations and thanks for the effort.");
            } else {
                msg.setText("Hey everybody, \n\n" +
                        "This is to notify you that you have not been awarded with the bonus for the " + lectureName + " course");
            }
            Transport.send(msg);
        } catch (MessagingException | UnsupportedEncodingException ignored) {

        }
    }
}
