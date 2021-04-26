<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="person" type="org.synyx.urlaubsverwaltung.person.Person" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>
<%@attribute name="nameIsNoLink" type="java.lang.Boolean" required="false" %>
<%@attribute name="noPadding" type="java.lang.Boolean" required="false" %>

<uv:person-box__ person="${person}" nameIsNoLink="${nameIsNoLink}" cssClass="${paddingCssClass} ${cssClass}" />
