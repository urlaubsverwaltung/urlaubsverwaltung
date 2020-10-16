<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="account" type="org.synyx.urlaubsverwaltung.account.Account" required="true" %>
<%@attribute name="className" type="java.lang.String" required="false" %>

<uv:account-entitlement-box__ account="${account}" className="tw-p-0 ${className}" />
