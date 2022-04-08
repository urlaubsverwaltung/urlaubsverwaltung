Hallo ${recipientNiceName},

leider ist dein Resturlaub zum 01.04.${year?c} in Höhe von ${expiredRemainingVacationDays?c} Tag(en) verfallen.

Dein aktueller Urlaubsanspruch:
    ${totalLeftVacationDays?c} Tag(e)
<#if remainingVacationDaysNotExpiring gt 0>
Resturlaub, der nicht am 01.04.${year?c} verfallen ist:
    ${remainingVacationDaysNotExpiring?c} Tag(e)
</#if>

Mehr Informationen zu deinem Urlaubsanspruch findest du hier: ${baseLinkURL}web/person/${personId?c}/overview
