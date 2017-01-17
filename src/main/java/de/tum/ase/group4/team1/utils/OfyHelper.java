package de.tum.ase.group4.team1.utils;

import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.*;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * OfyHelper, a ServletContextListener, is setup in web.xml to run before a JSP is run.  This is
 * required to let JSP's access Ofy.
 **/
public class OfyHelper implements ServletContextListener {
  public void contextInitialized(ServletContextEvent event) {
    // This will be invoked as part of a warmup request, or the first user request if no warmup
    // request.
    ObjectifyService.register(Semester.class);
    ObjectifyService.register(Lecture.class);
    ObjectifyService.register(ExerciseGroup.class);
    ObjectifyService.register(Enrollment.class);
    ObjectifyService.register(Session.class);
    ObjectifyService.register(Attendance.class);
  }

  public void contextDestroyed(ServletContextEvent event) {
    // App Engine does not currently invoke this method.
  }
}
