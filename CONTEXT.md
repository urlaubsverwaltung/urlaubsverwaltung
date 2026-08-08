# Context

The ubiquitous language of the Urlaubsverwaltung domain. German (`messages.properties`,
`MailMessages_de.properties`) is the source of truth; every other bundle is a translation of it.

Each entry gives the German term, what it means, and the one word each locale uses for it.
When a German term appears in a message, its translation uses the canonical word below —
not a synonym.

## Absence and leave

**Abwesenheit** — any period a person is not working, whatever the reason: leave, sick note,
home office, training. The umbrella concept. Not the request for it, and not a specific type.
→ en: *absence* · el: *απουσία*

**Antrag** — the request a person submits to have an Abwesenheit approved. Distinct from the
Abwesenheit itself: the Antrag is the document that goes through the approval workflow.
→ en: *application* (a pending one is an *absence request*) · el: *αίτημα*

**Urlaub** — leave drawn from a person's annual entitlement. A *kind* of Abwesenheit, never a
synonym for it.
→ en: *vacation* (*leave* inside compound absence-type names) · el: *άδεια*

**Erholungsurlaub** — ordinary annual leave, the default Urlaub type.
→ en: *Holiday* · el: *Κανονική άδεια*

**Resturlaub** — entitlement carried over from the previous year, which expires on a
configured date.
→ en: *remaining vacation* · el: *υπόλοιπο άδειας*

**Urlaubsanspruch** — how much Urlaub a person is entitled to in a year.
→ en: *vacation entitlement* · el: *δικαίωμα άδειας*

**Vertretung** — the colleague covering someone's work during an Abwesenheit.
→ en: *replacement* · el: *αντικατάσταση*

**Stornieren** — withdrawing an Abwesenheit or Antrag. The domain has no delete: records are
cancelled, never removed.
→ en: *cancel* · el: *ακύρωση*

## Sickness

**Krankmeldung** — the record that a person was sick, optionally backed by a doctor's
certificate. A kind of Abwesenheit, tracked separately from Urlaub and not deducted from
entitlement.
→ en: *sick note* · el: *σημείωμα ασθενείας*

Exception: on the absence-overview calendar the legend names the day's *state*, not the
document, so `absences.overview.sick*` reads *Sickness* / *Ασθένεια*. This is deliberate.

**Lohnfortzahlung** — the period an employer keeps paying a sick employee, capped by law.
→ en: *continued pay* · el: *συνέχιση της αμοιβής*

## People

**Person** — a human being in the system, independent of role or employment.
→ en: *person* · el: *άτομο*

**Mitarbeiter / Mitarbeitende** — a person considered as staff of the company.
→ en: *employee* · el: *εργαζόμενος*

**Benutzer** — a person considered as an account holder with permissions and a login.
→ en: *user* · el: *χρήστης*

`Mitarbeitende` and `Benutzer` often denote the same human. Which word the German uses signals
which aspect matters in that message; the translation follows it rather than picking one word
for both.

## Roles

**Abteilungsleiter** — approves absences for the members of their departments.
→ en: *Department Head* · el: *Προϊστάμενος τμήματος*

**Freigabe-Verantwortlicher** — grants the final approval in the two-stage approval process.
→ en: *Second Stage Authority* · el: *Υπεύθυνος έγκρισης*

**Chef** — approves absences for everyone, across all departments.
→ en: *Boss* · el: *Αφεντικό*

**Office** — administers the application: settings, all person data, absences and sick notes.
Kept untranslated in English; el: *Γραφείο*.

## Overtime

**Überstunden** — hours worked beyond contracted working time, accumulated on a balance.
→ en: *overtime* (uncountable — never *overtimes*) · el: *υπερωρίες*

**Überstundenabbau** — taking time off against the overtime balance. A kind of Abwesenheit.
→ en: *overtime reduction* · el: *μείωση υπερωριών*

## Conventions

- English uses **British spelling**: *-ise* / *-isation*, *cancelled*, *authorised*.
- German addresses the reader informally (*du*); English uses *you*, Greek the formal *σας*.
- German is gender-neutral (*Mitarbeitende*, *Kolleg:innen*); English uses *they/their*, never
  *he/his*.
- Mail body fragments follow the greeting line and therefore start lowercase, as in the German.
- `*.max_length` values are column widths for plain-text mail. They must equal the longest
  label in that group **in that language** — recompute them when a padded label changes.
- `{0, choice, …}` is ICU syntax. `choice`, `number` and `date` are keywords and are never
  translated.
