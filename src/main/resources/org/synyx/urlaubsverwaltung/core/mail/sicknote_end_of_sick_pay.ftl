Hallo ${recipient.niceName},

Die Krankmeldung von ${sickNote.person.niceName} für den Zeitraum ${sickNote.startDate.format("dd.MM.yyyy")} - ${sickNote.endDate.format("dd.MM.yyyy")} erreicht in Kürze die ${maximumSickPayDays} Tage Grenze.

Für Details siehe: ${baseLinkURL}web/sicknote/${sickNote.id?c}

Hinweis:
Der Anspruch auf Lohnfortzahlung durch den Arbeitgeber im Krankheitsfall besteht für maximal ${maximumSickPayDays} Tage
(fortlaufende Kalendertage ohne Rücksicht auf die Arbeitstage des erkrankten Arbeitnehmers, Sonn- oder Feiertage).
Danach wird für gesetzlich Krankenversicherte in der Regel Krankengeld von der Krankenkasse gezahlt.
