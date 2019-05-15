Hallo ${application.person.niceName},

${application.applier.niceName} hat deine Krankmeldung zu Urlaub umgewandelt.
Es handelt sich um folgenden Zeitraum: ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}

Für Details siehe: ${settings.baseLinkURL}web/application/${application.id?c}
