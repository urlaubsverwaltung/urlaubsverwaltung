<%-- 
    Document   : login
    Created on : 31.10.2011, 10:00:23
    Author     : Johannes Reuter + Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

    <head>
        <uv:head />
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/login.css' />" />
        <script type="text/javascript">
            $(document).ready(function() {
                    
                var url = document.URL;
            
                if(url.indexOf("login_error") != -1) {
                    $('#login-error').show('drop', {direction: "up"}); 
                } 
            });
        </script>
    </head>

    <body>


        <div class="navbar navbar-inverse navbar-fixed-top">
            <div class="navbar-inner">
                <div class="container_12">
                    <div class="grid_12">
                        <a class="brand" href="#">Urlaubsverwaltung</a>
                    </div>
                </div>
            </div>
        </div>

        <div class="container">

            <form class="form-signin" method="post" action="j_spring_security_check">
                <label for="j_username">Username</label>
                <input class="input-block-level" type="text" name="j_username" id="j_username" autofocus="autofocus">

                <label for="j_password">Passwort</label>
                <input class="input-block-level" type="password" name="j_password" id="j_password">
                
                <button class="btn btn-large btn-primary" type="submit">Login</button>

                <div id="login-error" style="display:none">
                    Der eingegebene Nutzername oder das Passwort ist falsch.
                </div>
            </form>

        </div>

    </body>

</html>
