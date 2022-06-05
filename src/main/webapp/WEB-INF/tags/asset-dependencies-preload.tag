<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="asset" type="java.lang.String" required="true" %>

<c:forEach items="${assets.get(asset)}" var="preload">
<link rel="preload" as="${preload.as}" href="${preload.href}" crossorigin>
</c:forEach>
