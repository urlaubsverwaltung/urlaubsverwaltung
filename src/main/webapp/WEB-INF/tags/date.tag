<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<%@attribute name="date" type="org.joda.time.DateMidnight" required="true" %>

<joda:format pattern="dd.MM.yyyy" value="${date}" />