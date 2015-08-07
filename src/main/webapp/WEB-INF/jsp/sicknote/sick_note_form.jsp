<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<html>
<head>
    <uv:head />

    <script src="<spring:url value='/js/datepicker.js' />" type="text/javascript" ></script>

    <script type="text/javascript">
        $(document).ready(function() {

            var person = '${param.person}';
            $("#employee").val(person)

            <%-- DATEPICKER --%>

            var datepickerLocale = "${pageContext.response.locale.language}";
            var urlPrefix = "<spring:url value='/api' />";

            <c:choose>
            <c:when test="${sickNote.id == null}">
                var getPersonId = function() {
                    return $("#employee option:selected").val();
                }
            </c:when>
            <c:otherwise>
                var getPersonId = function() {
                    var personId = "<c:out value="${sickNote.person.id}" />";
                    return personId;
                }
            </c:otherwise>
            </c:choose>

            var onSelect = function(selectedDate) {
                if (this.id == "from" && $("#to").val() === "") {
                    $("#to").datepicker("setDate", selectedDate);
                }
            };

            var onSelectAUB = function(selectedDate) {
                if (this.id == "aubFrom" && $("#aubTo").val() === "") {
                    $("#aubTo").datepicker("setDate", selectedDate);
                }
            };

            createDatepickerInstances(["#from", "#to"], datepickerLocale, urlPrefix, getPersonId, onSelect);
            createDatepickerInstances(["#aubFrom", "#aubTo"], datepickerLocale, urlPrefix, getPersonId, onSelectAUB);

            <%-- DATEPICKER END --%>

        });
        
    </script>
    
</head>
<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu />

<div class="content">

    <div class="container">

        <div class="row">

            <div class="col-xs-12">

                <div class="header">
                    <legend>
                        <p>
                            <c:choose>
                                <c:when test="${sickNote.id == null}">
                                    <spring:message code="sicknotes.new" />
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="sicknotes.edit" />
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </legend>
                </div>

            </div>

            <c:choose>
                <c:when test="${sickNote.id == null}">
                    <c:set var="METHOD" value="POST" />
                    <c:set var="ACTION" value="${URL_PREFIX}/sicknote" />
                </c:when>
                <c:otherwise>
                    <c:set var="METHOD" value="PUT" />
                    <c:set var="ACTION" value="${URL_PREFIX}/sicknote/${sickNote.id}/edit" />
                </c:otherwise>
            </c:choose>

            <form:form method="${METHOD}" action="${ACTION}" modelAttribute="sickNote" class="form-horizontal">

            <div class="col-xs-12">
              <c:set var="formErrors"><form:errors/></c:set>
              <c:if test="${not empty errors && not empty formErrors}">
                <div class="alert alert-danger">
                  <form:errors/>
                </div>
              </c:if>
            </div>

            <div class="col-xs-12 col-md-6">

                <div class="form-group">
                    <label class="control-label col-md-4" for="employee"><spring:message code='staff'/></label>

                    <div class="col-md-6">
                        <c:choose>
                            <c:when test="${sickNote.id == null}">
                                <form:select path="person" id="employee" class="form-control" cssErrorClass="form-control error">
                                    <c:forEach items="${persons}" var="person">
                                        <c:choose>
                                            <c:when test="${sickNote.person.id == person.id}">
                                                <form:option value="${person.id}" selected="selected">${person.niceName}</form:option>
                                            </c:when>
                                            <c:otherwise>
                                                <form:option value="${person.id}">${person.niceName}</form:option>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </form:select>
                                <span class="help-inline"><form:errors path="person" cssClass="error"/></span>
                            </c:when>
                            <c:otherwise>
                                <form:hidden path="id" />
                                <form:hidden path="person" value="${sickNote.person.id}" />
                                <c:out value="${sickNote.person.niceName}" />
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="form-group">
                    <label class="control-label col-md-4" for="sickNoteType"><spring:message code='type'/></label>

                    <div class="col-md-6">
                        <form:select path="type" id="sickNoteType" class="form-control" cssErrorClass="form-control error">
                            <c:forEach items="${sickNoteTypes}" var="type">
                                <form:option value="${type}"><spring:message code="sicknote.type.${type}" /></form:option>
                            </c:forEach>
                        </form:select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="control-label col-md-4" for="from"><spring:message code="sicknotes.time" /></label>

                    <div class="col-md-6">
                        <spring:message code="From" />
                        <form:input id="from" path="startDate" class="form-control" cssErrorClass="form-control error" />
                        <span class="help-inline"><form:errors path="startDate" cssClass="error"/></span>
                        <br />
                        <spring:message code="To" />
                        <form:input id="to" path="endDate" class="form-control" cssErrorClass="form-control error" />
                        <span class="help-inline"><form:errors path="endDate" cssClass="error"/></span>
                    </div>
                </div>

            </div>

            <div class="col-xs-12 col-md-6">

                <div class="form-group AU">
                    <div class="col-md-6">
                      <label class="control-label" for="aubFrom">
                        <spring:message code="sicknotes.aub.time" />
                      </label>
                      <span class="help-block"><spring:message code="sicknotes.aub.time.note" /></span>
                    </div>

                    <div class="col-md-6">
                        <spring:message code="From" />
                        <form:input id="aubFrom" path="aubStartDate" class="form-control" cssErrorClass="form-control error" />
                        <span class="help-inline"><form:errors path="aubStartDate" cssClass="error"/></span>
                        <br />
                        <spring:message code="To" />
                        <br />
                        <form:input id="aubTo" path="aubEndDate" class="form-control" cssErrorClass="form-control error" />
                        <span class="help-inline"><form:errors path="aubEndDate" cssClass="error"/></span>
                    </div>
                </div>

            </div>

            <div class="col-xs-12">

                <hr/>

                <button class="btn btn-success col-xs-12 col-sm-5 col-md-2" type="submit">
                    <spring:message code="action.save"/>
                </button>
                
                <button class="btn btn-default back col-xs-12 col-sm-5 col-md-2 pull-right" type="button">
                    <spring:message code="action.cancel"/>
                </button>

            </div>

        </div>

      </form:form>
            
    </div>
    
</div>    

</body>
</html>