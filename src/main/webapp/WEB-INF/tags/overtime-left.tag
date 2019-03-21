<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="hours" type="java.math.BigDecimal" required="true" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>

<div class="box ${cssClass}">
    <span class="box-icon bg-green hidden-print">
        <i class="fa fa-sort-amount-desc" aria-hidden="true"></i>
    </span>
    <span class="box-text">
        <spring:message code="overtime.person.left" arguments="${hours}"/>
    </span>
</div>
