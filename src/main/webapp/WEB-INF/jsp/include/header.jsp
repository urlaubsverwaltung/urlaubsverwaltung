<%-- 
    Document   : header
    Created on : 19.10.2011, 15:21:35
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
        <title>JSP Page</title>
    </head>
    
    <body>
        
        <!-- geklaut von redmine -->
        
        <div id="header">

            <h1>Urlaubsverwaltung</h1>


            <div id="main-menu">
                <ul><li><a class="overview" href="/projects/vacation_management">Startseite</a></li>
                    <li><a class="activity" href="/projects/vacation_management/activity">Urlaub beantragen</a></li>
                    <li><a class="issues" href="/projects/vacation_management/issues">Übersicht Mitarbeiter</a></li>
                    <li><a class="settings" href="/projects/vacation_management/settings">Einstellungen</a></li>
                </ul>
            </div>

        </div>


        <!-- zugehoeriger style    -->

        <!--#header, #top-menu {
            margin: 0;
        }

        #header {
            background: url("../images/logo.png") no-repeat scroll 0 50% #DBDDDE;
            border-bottom: 1px solid grey;
            height: 50px;
            padding-bottom: 10px;
            padding-left: 262px;
            padding-top: 40px;
        }

        #header {
            background-color: #507AAA;
            color: #F8F8F8;
            padding: 4px 8px 0 6px;
            position: relative;
        }

        body {
            font-family: Verdana,sans-serif;
            font-size: 12px;
        }-->
        
    </body>
</html>
