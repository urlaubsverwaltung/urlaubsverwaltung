<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@attribute name="number" type="java.math.BigDecimal" required="true" %>

<%-- TODO: This is sooo dirty... :( --%>
<fmt:formatNumber maxFractionDigits="1" value="${number}" var="formatted" />
${fn:replace(formatted, ".", ",")}