<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="year" type="java.lang.Integer" required="true" %>

<div class="btn-group pull-right">

    <button class="btn btn-default dropdown-toggle" data-toggle="dropdown">
        <i class="fa fa-clock-o"></i>
        <span class="hidden-xs"><spring:message code="action.change.year" /></span> <span class="caret"></span>
    </button>

    <ul class="dropdown-menu">
        
        <%-- next year --%>
        <li>
            <a href="?year=${year + 1}">
                <c:out value="${year + 1}" />
            </a>
        </li>
            
        <%-- current year --%>
            <li>
                <a href="?year=${year}">
                    <c:out value="${year}" />
                </a>
            </li>    
            
        <%-- last ten years --%>
       <c:forEach var="count" begin="1" end="10">
           <li>
               <a href="?year=${year - count}">
                   <c:out value="${year - count}"/>
               </a>
           </li>
       </c:forEach>

    </ul>

</div>