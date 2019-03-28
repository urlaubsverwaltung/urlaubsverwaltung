<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@attribute name="number" type="java.math.BigDecimal" required="true" %>

<fmt:formatNumber maxFractionDigits="2" value="${number}" />
