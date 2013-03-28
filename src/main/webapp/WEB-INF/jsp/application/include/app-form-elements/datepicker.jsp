<%-- 
    Document   : datepicker
    Created on : 06.09.2012, 17:15:21
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<script type="text/javascript">
    $(function() {
        $.datepicker.regional['en'] = {
            closeText: 'Done',
            prevText: 'Prev',
            nextText: 'Next',
            currentText: 'Today',
            monthNames: ['January', 'February', 'March', 'April', 'May', 'June',
                'July', 'August', 'September', 'October', 'November', 'December'],
            monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
            dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
            dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
            dayNamesMin: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'],
            weekHeader: 'Wk',
            dateFormat: 'dd.mm.yy',
            firstDay: 1,
            isRTL: false,
            showMonthAfterYear: false,
            yearSuffix: ''
        };
        $.datepicker.regional['de'] = {
            closeText: 'schließen',
            prevText: 'Zurück',
            nextText: 'Vor',
            currentText: 'heute',
            monthNames: ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
                'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'],
            monthNamesShort: ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun',
                'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'],
            dayNames: ['Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'],
            dayNamesShort: ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'],
            dayNamesMin: ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'],
            weekHeader: 'Wo',
            dateFormat: 'dd.mm.yy',
            firstDay: 1,
            isRTL: false,
            showMonthAfterYear: false,
            yearSuffix: ''
        };
        $.datepicker.setDefaults($.datepicker.regional["${pageContext.request.locale.language}"]);
        var dates = $("#from, #to, #at").datepicker({
            numberOfMonths: 1,
            beforeShowDay: function(date) {
                // if day is saturday or sunday, highlight it
                if(date.getDay()== 6 || date.getDay()== 0){
                    return [true,"notworkday"];
                } else {
                    // if date is a work day, check if it is a public holiday
                    // if so highlight it
                    var prefix = "<spring:url value='/web/calendar/public-holiday' />";
                    var dateString = date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + date.getDate();
                    var url = prefix + "?date=" + dateString;

                    var result = null


                    $.ajax({ 
                        url: url,
                        async: false,
                        success: function(data) {
                            result = data;
                        }
                    });

                    if(result == "0") {
                        return [true,""];
                    } else {
                        return [true,"notworkday"];
                    }
                    
                }  
                
            },
            onSelect: function(selectedDate) {
                instance = $(this).data("datepicker"),
                        date = $.datepicker.parseDate(
                        instance.settings.dateFormat ||
                        $.datepicker._defaults.dateFormat,
                        selectedDate, instance.settings);

                if (this.id == "from") {
                    $("#to").datepicker("setDate", date);
                }

                var prefix = "<spring:url value='/web/calendar/vacation' />";
                sendGetDaysRequest(prefix);
            }
        });
    });
</script>

<script type="text/javascript">
    $(document).ready(function() {
        $("input:radio[name=howLong]").change(function() {
            var urlPrefix = "<spring:url value='/web/calendar/vacation' />";
            sendGetDaysRequest(urlPrefix);
        });
    });
</script>