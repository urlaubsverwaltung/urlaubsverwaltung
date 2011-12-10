<%-- 
    Document   : login
    Created on : 31.10.2011, 10:00:23
    Author     : Johannes Reuter
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
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/login.css' />" /> 
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" /> 
    </head>

    <body>

        <div id="top-menu">
            Login
        </div>

        <div id="header">

            <h1 style="top:50%;
                left:50%;">Urlaubsverwaltung</h1>

        </div>

        <div id="login-content">

            <div id="wrapper">
                <%
                    if (request.getParameter("login_error") != null) {
                        out.println("Der Benutzer ist fehlerhaft.");
                    }

                %>
                
                <form method="post" action="j_spring_security_check">
                    
                    <table id="login-tbl">
                        <tr>
                            <td>
                                <label for="j_username">Username</label>
                            </td>
                            <td>
                                <input type="text" name="j_username" id="j_username" />
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label for="j_password">Password</label>
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
                               <input type='checkbox' name='_spring_security_remember_me' /> Remember me on this computer.
                            </td>
                        </tr>
                        <tr>
                            <td>
                                &nbsp;
                            </td>
                            <td style="padding-top: 1em;">
                               <input type="submit" value="Login" name="Login" style="float: right;" />
                            </td>
                        </tr>
                    </table>
                </form>
            </div>

        </div>    
    </body>

</html>
