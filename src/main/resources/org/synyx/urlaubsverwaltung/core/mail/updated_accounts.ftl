Stand Resturlaubstage zum 1. Januar ${today.toString("yyyy")} (mitgenommene Resturlaubstage aus dem Vorjahr)

<#list accounts as account>
${account.person.niceName}: ${account.remainingVacationDays}
</#list>
