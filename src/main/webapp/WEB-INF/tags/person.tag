<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="person" type="org.synyx.urlaubsverwaltung.person.Person" required="true" %>
<%@attribute name="departments" type="java.util.List<org.synyx.urlaubsverwaltung.department.Department>" required="false" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>
<%@attribute name="nameIsNoLink" type="java.lang.Boolean" required="false" %>
<%@attribute name="noPadding" type="java.lang.Boolean" required="false" %>

<c:set var="paddingCssClass" value="${noPadding ? 'tw-p-0' : 'tw-p-5'}" />

<uv:person-box__ person="${person}" departments="${departments}" nameIsNoLink="${nameIsNoLink}" cssClass="${paddingCssClass} ${cssClass}" />
