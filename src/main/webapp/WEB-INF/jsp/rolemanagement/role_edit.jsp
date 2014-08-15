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
        <style type="text/css">
            .app-detail .td-name {
                width: 20%;
                padding-right: 1.5em;
            }
        </style>
    </head>

    <body>

        <uv:menu />

        <spring:url var="formUrlPrefix" value="/web" />


        <div class="content">
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
                        </table>
                        
                    </div>
                    
                    <div class="grid_12">&nbsp;</div>
                    <div class="grid_12">&nbsp;</div>
                    
                    <div class="grid_8">

                        <div class="overview-header">

                            <legend>
                                <p>
                                    <spring:message code='role'/>
                                </p>
                            </legend>

                        </div>
                    
                        <div class="control-group">
                            <div class="controls">
                                <label class="checkbox">
                                    <form:checkbox path="permissions" value="INACTIVE" />&nbsp;<b><spring:message code="role.inactive" /></b>:
                                    <spring:message code="role.inactive.dsc" />
                                </label>
                            </div>
                            <div class="controls">
                                <label class="checkbox">
                                    <form:checkbox path="permissions" value="USER" />&nbsp;<b><spring:message code="role.user" /></b>:
                                    <spring:message code="role.user.dsc" />
                                </label>
                            </div>
                            <div class="controls">
                                <label class="checkbox">
                                    <form:checkbox path="permissions" value="BOSS" />&nbsp;<b><spring:message code="role.boss" /></b>:
                                    <spring:message code="role.boss.dsc" />
                                </label>
                            </div>
                            <div class="controls">
                                <label class="checkbox">
                                    <form:checkbox path="permissions" value="OFFICE" />&nbsp;<b><spring:message code="role.office" /></b>:
                                    <spring:message code="role.office.dsc" />
                                </label>
                            </div>
                            <div class="controls">
                                <label class="checkbox">
                                    <form:checkbox path="permissions" value="ADMIN" />&nbsp;<b><spring:message code="role.admin" /></b>:
                                    <spring:message code="role.admin.dsc" />
                                </label>
                            </div>

                            <c:if test="${not empty msg}">
                                <br />
                                <span class="error">
                                    <spring:message code="${msg}" />
                                </span>
                            </c:if>
                            
                        </div>
                    </div>

                    <div class="grid_8">
                        
                        <hr />

                        <button class="btn" type="submit"><i class='icon-ok'></i>&nbsp;<spring:message code="save" /></button>
                        <a class="btn" href="${formUrlPrefix}/management"><i class='icon-remove'></i>&nbsp;<spring:message code='cancel'/></a>
                        
                    </div>

                </form:form>

            </div> 
        </div>    

    </body>

</html>
