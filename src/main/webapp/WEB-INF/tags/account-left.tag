<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<%@attribute name="account" type="org.synyx.urlaubsverwaltung.account.Account" required="true" %>
<%@attribute name="vacationDaysLeft" type="org.synyx.urlaubsverwaltung.account.VacationDaysLeft" required="true" %>
<%@attribute name="beforeApril" type="java.lang.Boolean" required="true" %>
<%@attribute name="className" type="java.lang.String" required="false" %>

<uv:account-left-box__
    account="${account}"
    vacationDaysLeft="${vacationDaysLeft}"
    beforeApril="${beforeApril}"
    className="tw-p-5 ${className}"
/>
