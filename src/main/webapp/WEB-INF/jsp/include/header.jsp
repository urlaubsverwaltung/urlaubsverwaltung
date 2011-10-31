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

       
        <!--  noch absolut provisorisch!   -->


        <spring:url var="formUrlPrefix" value="/web/urlaubsverwaltung" />
        
        <!-- geklaut von redmine - Anpassen!! -->

        <div id="top-menu">
            <spring:message code="loggedas" />&nbsp;<c:out value="${user.login}" />     
        </div>
        
        <div id="header">
            
            <h1>Urlaubsverwaltung</h1>

            <div id="main-menu">
                
            <!-- muss noch vernünftig angepasst werden -->
                
                <c:if test="normalerUser">
                <ul>
                    <li><a href="${formUrlPrefix}/mitarbeiter/${person.id}/overview">Übersicht</a></li>
                    <li><a href="${formUrlPrefix}/antrag/${person.id}/new">Urlaub beantragen</a></li>
                </ul>
                </c:if>
            
                <c:if test="chef">
                <ul><li><a href="${formUrlPrefix}/mitarbeiter/${person.id}/overview">Übersicht</a></li>
                    <li><a href="${formUrlPrefix}/antrag/${person.id}/new">Urlaub beantragen</a></li>
                    <li><a href="${formUrlPrefix}/antraege/wartend">Wartende Anträge</a></li>
                    <li><a href="${formUrlPrefix}/antraege/genehmigt">Genehmigte Anträge</a></li>
                    <li><a href="${formUrlPrefix}/antraege/storniert">Stornierte Anträge</a></li>
                    <li><a href="${formUrlPrefix}/mitarbeiter/list">Übersicht Mitarbeiter</a></li>
                </ul>
                </c:if>
            
                <c:if test="office">
                <ul><li><a href="${formUrlPrefix}/mitarbeiter/${person.id}/overview">Übersicht</a></li>
                    <li><a href="${formUrlPrefix}/antrag/${person.id}/new">Urlaub beantragen</a></li>
                    <li><a href="${formUrlPrefix}/antraege/genehmigt">Genehmigte Anträge</a></li>
                    <li><a href="${formUrlPrefix}/antraege/storniert">Stornierte Anträge</a></li>
                    <li><a href="${formUrlPrefix}/mitarbeiter/list">Übersicht Mitarbeiter</a></li>
                    <li><a href="${formUrlPrefix}/manager">Manager Bereich</a></li>
                </ul>
                </c:if>
                
            </div>

        </div>


        <!-- zugehoeriger style  mal ins css klatschen irgendwann  -->

        <!--#header, #top-menu {
            margin: 0;
        }
        
        #top-menu {
            background: none repeat scroll 0 0 #444444;
            color: #FFFFFF;
            font-size: 0.8em;
            height: 1.8em;
            padding: 2px 2px 0 6px;
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
        
        #header a {
        color: #505050;
        }

        #main-menu li a {
            -moz-border-bottom-colors: none;
            -moz-border-image: none;
            -moz-border-left-colors: none;
            -moz-border-right-colors: none;
            -moz-border-top-colors: none;
            background-color: #CCCCCC;
            border-color: #AAAAAA #AAAAAA #CCCCCC;
            border-style: solid;
            border-width: 1px;
            color: #444444;
            font-weight: bold;
        }

        #main-menu li a:hover {
            background: none repeat scroll 0 0 #CCCCCC;
            color: #0083CC;
            text-decoration: underline;
        }

        #main-menu li a.selected, #main-menu li a.selected:hover {
            background-color: grey;
            border: 1px solid grey;
            color: white;
        }

        body {
            font-family: Verdana,sans-serif;
            font-size: 12px;
        }-->
        
