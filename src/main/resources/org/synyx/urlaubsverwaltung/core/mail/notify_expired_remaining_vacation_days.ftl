Hallo ${recipientNiceName},

leider ist dein Resturlaub zum ${expiryDate.format("dd.MM.yyyy")} in Höhe von ${expiredRemainingVacationDays?c} Tag(en) verfallen.

Dein aktueller Urlaubsanspruch:
    ${totalLeftVacationDays?c} Tag(e)
<#if remainingVacationDaysNotExpiring gt 0>
Resturlaub, der nicht am ${expiryDate.format("dd.MM.yyyy")} verfallen ist:
    ${remainingVacationDaysNotExpiring?c} Tag(e)
</#if>

Mehr Informationen zu deinem Urlaubsanspruch findest du hier: ${baseLinkURL}web/person/${personId?c}/overview
