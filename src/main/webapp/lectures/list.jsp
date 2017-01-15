<%@ page import="com.googlecode.objectify.ObjectifyService" %>
<%@ page import="de.tum.ase.group4.team1.models.Semester" %>
<%@ page import="java.util.List" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    List<Semester> semesters = ObjectifyService.ofy().load().type(Semester.class).list();
    if (!semesters.isEmpty()) {
        request.setAttribute("semesters", semesters);
    } else {
        request.setAttribute("no_semesters_found", true);
    }
%>

<t:base>
    <jsp:attribute name="title">Lectures</jsp:attribute>
    <jsp:body>
        <div class="row">
            <div class="col-md-3">
                <ul>
                    <c:if test="${no_semesters_found}">
                        <li class="text-muted">No semesters found</li>
                    </c:if>
                    <c:forEach var="semester" items="${semesters}">
                        <li>${semester.title}</li>
                    </c:forEach>
                </ul>
            </div>
            <div class="col-md-9">
                <table class="table">
                    <tr><td colspan="2" class="text-center text-muted">
                        No lectures found. Please create one <a href="/lectures/create">here</a>
                    </td></tr>
                </table>
                <div class="float-right">
                    <a href="/lectures/create" class="btn btnsuccess"><i class="fa fa-plus" aria-hidden="true"></i></a>
                </div>
            </div>
        </div>
    </jsp:body>
</t:base>
