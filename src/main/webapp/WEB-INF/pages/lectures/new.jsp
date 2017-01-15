<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:base>
    <jsp:attribute name="title">Create new lecture</jsp:attribute>
    <jsp:body>
        <div class="row">
            <div class="col-sm-9 offset-sm-3">
                <form action="" method="POST">
                    <div class="form-group row">
                        <label for="semester" class="col-sm-3 col-form-label">Semester</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control"  id="semester" name="semester" placeholder="Semester" />
                        </div>
                    </div>
                    <div class="form-group row">
                        <div class="offset-sm-3 col-sm-9">
                            <button type="submit" class="btn btn-primary">Create</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </jsp:body>
</t:base>