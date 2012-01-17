<%-- 
    Document   : staff_detail
    Created on : 31.10.2011, 11:49:53
    Author     : Aljona Murygina
--%>
  
      <c:forEach items="${persons}" var="person">
        <table id="person-tbl" cellspacing="0" border="1">
            <tr>
                <td class="td-pic" rowspan="6"><img class="user-pic" src="<c:out value='${gravatarUrls[person]}?d=mm'/>" /></td>
            </tr>
            <tr>
                <th>
                   <spring:message code="staff" />: 
                </th>
                <td>
                   <c:out value="${person.lastName}"/>&nbsp;<c:out value="${person.firstName}"/> 
                </td>
            </tr>
            <tr>
                <th>
                   <spring:message code="entitlement" />&nbsp;<spring:message code="in.year" />&nbsp;<c:out value="${year}"/>
                </th>
                <td>
                    <c:out value="${entitlements[person].vacationDays + entitlements[person].remainingVacationDays}"/>&nbsp;<spring:message code="peryear" />
                    &nbsp;(<spring:message code="davon" />
                        <c:choose>
                            <c:when test="${entitlements[person].remainingVacationDays == null}">
                                0
                            </c:when>
                            <c:otherwise>
                                <c:out value="${entitlements[person].remainingVacationDays}"/>
                            </c:otherwise>
                        </c:choose>
                        <spring:message code="days" />&nbsp;<spring:message code="remaining" />)
                </td>
            </tr>
            <tr>
                <th>
                    <spring:message code="overview.used" />
                </th>
                <td>
                    <c:choose>
                            <c:when test="${accounts[person] != null}">
                                <c:out value="${(entitlements[person].vacationDays - accounts[person].vacationDays) + (entitlements[person].remainingVacationDays - accounts[person].remainingVacationDays)}"/>&nbsp;<spring:message code="days" />
                            </c:when>
                            <c:otherwise>
                                0 <spring:message code="days" />
                            </c:otherwise>
                        </c:choose>
                </td>
            </tr>
            <tr>
                <th>
                    <spring:message code="overview.left" />
                </th>
                <td>
                    <c:choose>
                            <c:when test="${accounts[person] != null}">
                                <c:choose>
                                    <c:when test="${april == 1}">
                                        <c:out value="${accounts[person].vacationDays + accounts[person].remainingVacationDays}"/>&nbsp;<spring:message code="days" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:out value="${accounts[person].vacationDays}"/>&nbsp;<spring:message code="days" />
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${entitlements[person].vacationDays + entitlements[person].remainingVacationDays}"/>&nbsp;<spring:message code="days" />
                            </c:otherwise>
                        </c:choose>
                </td>
            </tr>
            <tr>
                <th>&nbsp;</th>
                <td>
                    <a href="${formUrlPrefix}/${person.id}/application"><spring:message code="table.app" /></a>
                    <a href="${formUrlPrefix}/staff/${person.id}/edit"><spring:message code="edit" /></a>
                </td>
            </tr>   
        </table>    
        <br />        
        </c:forEach>

