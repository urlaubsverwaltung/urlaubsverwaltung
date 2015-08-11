<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<%@attribute name="dateTime" type="org.joda.time.DateTime" required="true" %>

<joda:format pattern="dd.MM.yyyy" value="${dateTime}" />