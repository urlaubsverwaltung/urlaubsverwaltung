<div id="content">

    <div class="container_12">

        <div class="grid_8"> 

            <table class="overview-header" style="margin-bottom: 0;">
                <tr>
                    <td colspan="2"><spring:message code="overview" />&nbsp;Admin</td>
                </tr>
            </table>

        </div>
        
        <div class="grid_12">&nbsp;</div>

        <div class="grid_2">
            <table class="app-detail" cellspacing="0" style="height: 13em; text-align: center">
                <tr class="odd">
                    <td rowspan="6" style="text-align: center">
                        <img class="user-pic" src="<c:out value='${gravatar}?d=mm&s=110'/>" /> 
                    </td>
                </tr>
            </table>
        </div>

        <div class="grid_6">
            <table class="app-detail" cellspacing="0">
                <tr class="odd">
                    <th><spring:message code='login' /></th>
                    <td><c:out value="${person.loginName}" /></td>  
                </tr>
                <tr class="even">
                    <th><spring:message code='name' /></th>
                    <td><c:out value="${person.firstName}" />&nbsp;<c:out value="${person.lastName}" /></td>
                </tr>
                <tr class="odd">
                    <th><spring:message code='email' /></th>
                    <td><c:out value="${person.email}" /></td>  
                </tr>
            </table>
        </div>           
    </div>


</div>

