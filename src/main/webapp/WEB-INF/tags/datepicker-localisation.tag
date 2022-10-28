<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<script>
    window.uv = window.uv || {};
    window.uv.datepicker = window.uv.datepicker || {};
    window.uv.datepicker.localisation = {
        locale: "${language}",
        buttonLabel: "<spring:message code="datepicker.buttonLabel" />",
        placeholder: "<spring:message code="datepicker.placeholder" />",
        selectedDateMessage: "<spring:message code="datepicker.selectedDateMessage" />",
        prevMonthLabel: "<spring:message code="datepicker.prevMonthLabel" />",
        nextMonthLabel: "<spring:message code="datepicker.nextMonthLabel" />",
        monthSelectLabel: "<spring:message code="datepicker.monthSelectLabel" />",
        yearSelectLabel: "<spring:message code="datepicker.yearSelectLabel" />",
        closeLabel: "<spring:message code="datepicker.closeLabel" />",
        keyboardInstruction: "<spring:message code="datepicker.keyboardInstructions" />",
        calendarHeading: "<spring:message code="datepicker.calendarHeading" />",
        dayNames: [
            "<spring:message code="SUNDAY" />",
            "<spring:message code="MONDAY" />",
            "<spring:message code="TUESDAY" />",
            "<spring:message code="WEDNESDAY" />",
            "<spring:message code="THURSDAY" />",
            "<spring:message code="FRIDAY" />",
            "<spring:message code="SATURDAY" />"
        ],
        monthNames: [
            "<spring:message code="month.january" />",
            "<spring:message code="month.february" />",
            "<spring:message code="month.march" />",
            "<spring:message code="month.april" />",
            "<spring:message code="month.may" />",
            "<spring:message code="month.june" />",
            "<spring:message code="month.july" />",
            "<spring:message code="month.august" />",
            "<spring:message code="month.september" />",
            "<spring:message code="month.october" />",
            "<spring:message code="month.november" />",
            "<spring:message code="month.december" />",
        ],
        monthNamesShort: [
            "<spring:message code="month.january.short" />",
            "<spring:message code="month.february.short" />",
            "<spring:message code="month.march.short" />",
            "<spring:message code="month.april.short" />",
            "<spring:message code="month.may.short" />",
            "<spring:message code="month.june.short" />",
            "<spring:message code="month.july.short" />",
            "<spring:message code="month.august.short" />",
            "<spring:message code="month.september.short" />",
            "<spring:message code="month.october.short" />",
            "<spring:message code="month.november.short" />",
            "<spring:message code="month.december.short" />",
        ],
    }
</script>
