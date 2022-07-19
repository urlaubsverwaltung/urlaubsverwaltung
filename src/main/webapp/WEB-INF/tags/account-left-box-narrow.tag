<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="account" type="org.synyx.urlaubsverwaltung.account.Account" required="true" %>
<%@attribute name="vacationDaysLeft" type="org.synyx.urlaubsverwaltung.account.VacationDaysLeft" required="true" %>
<%@attribute name="expiredRemainingVacationDays" type="java.math.BigDecimal" required="true" %>
<%@attribute name="expiryDate" type="java.time.LocalDate" required="true" %>
<%@attribute name="beforeExpiryDate" type="java.lang.Boolean" required="true" %>
<%@attribute name="className" type="java.lang.String" required="false" %>

<uv:account-left-box__
        account="${account}"
        vacationDaysLeft="${vacationDaysLeft}"
        expiredRemainingVacationDays="${expiredRemainingVacationDays}"
        expiryDate="${expiryDate}"
        beforeExpiryDate="${beforeExpiryDate}"
        className="tw-p-0 ${className}"
/>

