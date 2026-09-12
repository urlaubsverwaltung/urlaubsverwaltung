# "Available For Work" ist ein reines Anzeige-Flag

## Kontext

Wir ergänzen jede Abwesenheitsart (mitgeliefert und selbst angelegt) um ein konfigurierbares Boolean `availableForWork`. Es kennzeichnet Abwesenheiten, bei denen die Person an einem anderen Ort weiterarbeitet (z. B. Home Office, Außer Haus) und damit erreichbar bleibt — im Gegensatz zu einer echten Abwesenheit wie Erholungsurlaub.

Das System kennt kein explizites Konzept "anwesend vs. abwesend" — die bloße Existenz einer Abwesenheit bedeutete bisher immer "nicht bei der Arbeit". Wer das neue Flag sieht, könnte berechtigterweise annehmen, dass es auch Verfügbarkeitslogik, Kontingent-Verrechnung, Genehmigung oder Vertretungsregeln beeinflusst.

## Entscheidung

`availableForWork` wirkt sich **nur auf die Darstellung** aus. Es verändert, wie eine bestehende Abwesenheit angezeigt und geteilt wird — sonst nichts:

- Der iCal-Export markiert das Event als `TRANSPARENT` / `BUSYSTATUS=FREE`, statt zu blocken.
- Der Google-Kalender-Sync markiert das Event als transparent.
- Die Abwesenheitsübersicht (Grid) stellt den Tag als eigenen Zustand "anwesend, arbeitet woanders" dar, statt als normale Abwesenheit.
- Die Absence-REST-DTO exponiert das Flag.

Bewusst **nicht** betroffen sind: Urlaubskontingente/-verrechnung, Genehmigungs-Workflow, Vertretungs-/Holiday-Replacement-Regeln sowie jegliche "ist diese Person verfügbar"-Fachlogik. Die Person bleibt im Domänenmodell vollständig abwesend; nur die äußere Sicht ändert sich.

## Konsequenzen

- Kleinste, risikoärmste Änderung: keine Verrechnungs- oder Workflow-Logik ist betroffen.
- Das Flag liegt ausschließlich auf der Abwesenheitsart, daher gelten Krankmeldungen (eigene Art/Kategorie) immer als echt abwesend.
- Falls künftig eine Anforderung entsteht, dass "available for work" echte Verfügbarkeit oder Verrechnung beeinflussen soll, ist das eine additive Erweiterung obendrauf — bis dahin sollte niemand dieses Flag in Fachlogik verdrahten in der Annahme, es bedeute dort bereits etwas.