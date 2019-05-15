Stand Resturlaubstage zum 1. Januar ${today.format("yyyy")} (mitgenommene Resturlaubstage aus dem Vorjahr)

<#list accounts as account>
${account.person.niceName}: ${account.remainingVacationDays}
</#list>
