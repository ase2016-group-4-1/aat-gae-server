<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ tag import="com.google.appengine.api.users.UserService" %>
<%@ tag import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ tag import="com.google.appengine.api.users.User" %>

<%@ tag description="Base page template" pageEncoding="UTF-8" %>
<%@ attribute name="title" %>
<%@ attribute name="stylesheets" fragment="true" %>
<%@ attribute name="scripts" fragment="true" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

        <title><%= title %> - Automated Attendance Tracking</title>

        <!-- Bootstrap CSS -->
        <link rel="stylesheet" type="text/css" href="css/bootstrap.min.css" />
        <jsp:invoke fragment="stylesheets"/>
    </head>
    <body>
        <nav class="navbar navbar-toggleable-md navbar-light bg-faded mb-4">
            <div class="container">
                <button class="navbar-toggler navbar-toggler-right" type="button" data-toggle="collapse" data-target="#navbarCollapse" aria-controls="navbarCollapse" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <a class="navbar-brand" href="/">AAT</a>
                <div class="collapse navbar-collapse" id="navbarCollapse">

                    <%
                        String requestUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
                        UserService userService = UserServiceFactory.getUserService();
                        User user = userService.getCurrentUser();
                    %>
                    <c:set var="requestUri" value="<%= requestUri %>"></c:set>
                    <ul class="navbar-nav mr-auto">
                        <li class="nav-item ${requestUri == '/' ? 'active' : ''}">
                            <a class="nav-link" href="/">Home</a>
                        </li>
                        <li class="nav-item ${requestUri == '/lectures' ? 'active' : ''}">
                            <a class="nav-link" href="/lectures">Lectures</a>
                        </li>
                    </ul>
                    <ul class="navbar-nav">
                        <c:choose>
                            <c:when test="<%= userService.isUserLoggedIn() %>">
                                <li class="nav-item dropdown">
                                    <a class="nav-link dropdown-toggle" href="#" id="userDropdownMenuLink" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                        <%= user.getNickname() %>
                                    </a>
                                    <div class="dropdown-menu" aria-labelledby="userDropdownMenuLink">
                                        <c:if test="<%= userService.isUserAdmin() %>">
                                            <a class="dropdown-item" href="#">Admin</a>
                                        </c:if>
                                        <a class="dropdown-item" href="<%= userService.createLogoutURL(requestUri) %>">Sign out</a>
                                    </div>
                                </li>
                            </c:when>
                            <c:otherwise>
                                <li><a class="nav-link" href="<%= userService.createLoginURL(requestUri) %>">Sign in</a></li>
                            </c:otherwise>
                        </c:choose>
                    </ul>
                </div>
            </div>
        </nav>

        <div class="container">
            <jsp:doBody/>
        </div>

        <!-- jQuery first, then Tether, then Bootstrap JS. -->
        <script src="js/jquery-3.1.1.slim.min.js"></script>
        <script src="js/tether.min.js"></script>
        <script src="js/bootstrap.min.js"></script>

        <jsp:invoke fragment="scripts"/>
    </body>
</html>