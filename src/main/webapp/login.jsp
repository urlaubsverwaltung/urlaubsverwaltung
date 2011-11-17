<%-- 
    Document   : login
    Created on : 31.10.2011, 10:00:23
    Author     : aljona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!DOCTYPE html>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login</title>
        <style type="text/css">
            #wrapper {
                border: 2px solid #FFF95C;
                background-color: #FFF7A4;
                width:350px;
                height:130px;
                position:absolute;
                top:50%;
                left:50%;
                margin-left:-185px;
                margin-top:-75px;
                text-align:center;
                padding:20px;
            }
        </style>
    </head>

    <body>
        <div id="wrapper">
        <%
            if (request.getParameter("login_error") != null) {
                out.println("Der Benutzer ist fehlerhaft.");
            }

        %>
        <form method="post" action="j_spring_security_check">
            <label for="j_username">Username</label>
            <input type="text" name="j_username" id="j_username"/>
            <br/>
            <label for="j_password">Password</label>
            <input type="password" name="j_password" id="j_password"/>
            <br/>
            <input type='checkbox' name='_spring_security_remember_me'/> Remember me on this computer.
            <br/>
            <input type="submit" value="Login"/>
        </form>
        </div>
    </body>

</html>
