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


<!DOCTYPE html>
<html>

    <head>
        <title>Login</title>
        <%@include file="/WEB-INF/jsp/include/header.jsp" %>
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

        <div id="login-content">

            <div id="wrapper">

                <form method="post" action="j_spring_security_check">

                    <table id="login-tbl">
                        <tr>
                            <td style="text-align: left">
                                <label for="j_username">Username</label>
                            </td>
                            <td>
                                <input type="text" name="j_username" id="j_username" />
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align: left">
                                <label for="j_password">Passwort</label>
                            </td>
                            <td>
                                <input type="password" name="j_password" id="j_password" />
                            </td>
                        </tr>
                        <tr>
                            <td>
                                &nbsp;
                            </td>
                            <td style="padding-top: 1em;">
                                <input class="btn btn-primary" type="submit" value="Login" name="Login" style="float: right;" />
                            </td>
                        </tr>
                    </table>
                </form>

                <div id="login-error" style="display:none">
                    Der eingegebene Nutzername oder das Passwort ist falsch.
                </div>

            </div>

        </div>    
    </body>

</html>
