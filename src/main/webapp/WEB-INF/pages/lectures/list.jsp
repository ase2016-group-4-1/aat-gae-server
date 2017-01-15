<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<t:base>
    <jsp:attribute name="title">Lectures</jsp:attribute>
    <jsp:body>
        <div class="row">
            <div class="col-md-3">
                <ul>
                    <c:if test="${fn:length(semesters) == 0}">
                        <li class="text-muted">No semesters found</li>
                    </c:if>
                    <c:forEach var="semester" items="${semesters}">
                        <li>
                            <spring:url value="/lectures/{semesterSlug}" var="url">
                                <spring:param name="semesterSlug" value="${semester.slug}" />
                            </spring:url>
                            <a href="${url}">
                                ${semester.title}
                            </a>
                    </li>
                    </c:forEach>
                </ul>
            </div>
            <div class="col-md-9">
                <b>Lectures for ${selectedSemester.title}</b>
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
