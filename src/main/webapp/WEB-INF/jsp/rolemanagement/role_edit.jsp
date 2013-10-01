<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!DOCTYPE html>
<html>

    <head>
        <title><spring:message code="title" /></title>
        <%@include file="../include/header.jsp" %>
        <style type="text/css">
            .app-detail .td-name {
                width: 20%;
                padding-right: 1.5em;
            }
            
            td.roles {
                width: 10%;
            }
        </style>
    </head>

    <body>

        <%@include file="../include/menu_header.jsp" %>

        <spring:url var="formUrlPrefix" value="/web" />


        <div id="content">
            <div class="container_12">

                <form:form method="PUT" action="${formUrlPrefix}/management/${person.id}" modelAttribute="person"> 

                    <div class="grid_8">

                        <div class="overview-header">

                            <legend>
                                <p>
                                    <spring:message code="person.data" />
                                </p>
                            </legend>

                        </div>

                    </div>

                    <div class="grid_8">
                        <table class="app-detail" cellspacing="0">
                            <tr class="odd">
                                <td class="td-name"><spring:message code='login' />:</td>
                                <td colspan="2">
                                    <c:out value="${person.loginName}" />
                                </td>
                            </tr>
                            <tr class="even">
                                <td class="td-name"><spring:message code="firstname" />:</td>
                                <td colspan="2">
                                    <c:out value="${person.firstName}" />
                                </td>
                            </tr>
                            <tr class="odd">
                                <td class="td-name"><spring:message code="lastname" />:</td>
                                <td colspan="2">
                                    <c:out value="${person.lastName}" />
                                </td>
                            </tr>
                            <tr class="even">
                                <td class="td-name"><spring:message code="email" />:</td>
                                <td colspan="2">
                                    <c:out value="${person.email}" />
                                </td>
                            </tr>
                            <tr class="odd">
                                <td class="td-name">
                                    <spring:message code="role" />:
                                    <br /><br />
                                    <c:if test="${not empty msg}">
                                    <span class="error">
                                        <spring:message code="${msg}" />
                                    </span>
                                    </c:if>
                                </td>
                                <td class="roles">
                                    <form:checkbox path="permissions" value="INACTIVE" />&nbsp;<spring:message code="role.inactive" />
                                </td>
                                <td>
                                   <spring:message code="role.inactive.dsc" /> 
                                </td>
                            </tr>
                            <tr class="odd">
                                <td class="td-name">&nbsp;</td>
                                <td class="roles">
                                    <form:checkbox path="permissions" value="USER" />&nbsp;<spring:message code="role.user" />
                                </td>
                                <td>
                                   <spring:message code="role.user.dsc" /> 
                                </td>
                            </tr>
                            <tr class="odd">
                                <td class="td-name">&nbsp;</td>
                                <td class="roles">
                                    <form:checkbox path="permissions" value="BOSS" />&nbsp;<spring:message code="role.boss" />
                                </td>
                                <td>
                                   <spring:message code="role.boss.dsc" /> 
                                </td>
                            </tr>
                            <tr class="odd">
                                <td class="td-name">&nbsp;</td>
                                <td class="roles">
                                    <form:checkbox path="permissions" value="OFFICE" />&nbsp;<spring:message code="role.office" />
                                </td>
                                <td>
                                   <spring:message code="role.office.dsc" /> 
                                </td>
                            </tr>
                            <tr class="odd">
                                <td class="td-name">&nbsp;</td>
                                <td class="roles">
                                    <form:checkbox path="permissions" value="ADMIN" />&nbsp;<spring:message code="role.admin" />
                                </td>
                                <td>
                                   <spring:message code="role.admin.dsc" /> 
                                </td>
                            </tr>
                        </table>
                    </div>

                    <div class="grid_12">&nbsp;</div>
                    <div class="grid_8" style="background-color: #EAF2D3; height: 2em; padding-top: 1em; padding-bottom: 1em;">
                        &nbsp;

                        <input class="btn btn-primary" type="submit" name="<spring:message code="save" />" value="<spring:message code="save" />" />
                        <a class="btn" href="${formUrlPrefix}/management"><spring:message code='cancel' /></a>
                    </div>

                </form:form>

            </div> 
        </div>    

    </body>

</html>
