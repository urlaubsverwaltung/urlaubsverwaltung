# Architecture Decision Records

An ADR records one decision of this project, the reasons behind it and what it costs. ADRs are written for the reader
who has to follow the decision later, so they start with the decision itself. The format is based on the one described
by Andrew Harmel-Law in *Facilitating Software Architecture*, chapter 6.

## Conventions

- File name `YYYY-MM-DD-<slug>.md`. The date is the day the ADR was written and stays as it is, the slug is unique.
- The title states the decision, e.g. "Mark required form fields with the required attribute …" rather than
  "Required form fields". Read one after the other, the titles tell the history of decisions. Keep it short, it is
  repeated in the index.
- An accepted ADR is not rewritten, only its frontmatter changes. If the decision changes, write a new ADR and mark
  the old one as `superseded`.
- Start from [template.md](template.md).
- Where a rule of the decision can be checked automatically, add a test for it whose failure message names the ADR.

## Sections

- **Decision**: "We will …". The chosen option and what follows from it, without the problem, the alternatives or
  the reasons. It stands on its own: agents read only this section to follow the decision.
- **Context**: the situation and forces at the time of the decision, and the criteria to decide by.
- **Options**: a numbered list, the selected option first. There is always more than one, "do nothing" counts.
- **Consequences**: for each option why it was selected or rejected, and despite what. The drawbacks of the selected
  option are the most important part. Refer to the Context instead of repeating it.
- **Advice**: whom you asked (people affected by the decision and people who know the topic) and what each of them
  advised, with name, role and date. Advice does not have to be followed, but if important advice was not followed,
  the Consequences explain why.

## Frontmatter

```yaml
status: accepted   # see lifecycle
date: 2026-10-30   # date of the last status change
author: Jane Doe   # who is accountable for the decision and answers questions about it
superseded-by:     # only when superseded: file name of the ADR replacing this one
```

## Lifecycle

- `draft`: the ADR is being written. The Decision section stays empty while no option is selected.
- `proposed`: an option is selected for now, advice is being collected. The Decision section and the title state the
  selected option, since agents follow a proposed ADR as well. Open the ADR as a pull request, so advice can be given
  as review comments; copy it into the Advice section before the ADR is accepted.
- `accepted`: the decision is taken. Set the `date`, finish the Decision section and the title.
- `superseded`: a newer ADR replaces it, named in `superseded-by`. The newer ADR names it in its Context.
- `expired`: the decision was taken for a limited time, which is over; it needs to be revisited.
- `retired`: what the decision was about does not exist anymore, and nothing replaces it.

There is no `rejected`. Keeping things as they are is a decision as well: an accepted ADR with "do nothing" as the
selected option.

## Index

Agents load [AGENTS.md](../../AGENTS.md) in every session, but read an ADR only when their work matches its entry in
the "Decisions" section there:

- Every `proposed` and `accepted` ADR has an entry, no other: drafts are not decided yet, and superseded, expired and
  retired ADRs do not apply anymore. Add the entry when the ADR is proposed and remove it when it stops applying.
- An entry is the title as link, a colon and when the ADR applies, in domain terms rather than file paths. Agents
  match it against their task, so it names the situation of the work, not the solution the ADR selects. It may
  wrap onto indented lines. A proposed ADR is marked as such:
  `- (proposed) [<title>](docs/adr/<file name>): when …`
- `ArchitectureDecisionRecordsTest` checks this.

### When the index outgrows AGENTS.md

Every entry costs tokens in every agent session, whether the work touches its area or not. So the index stays in
AGENTS.md for up to 15 entries, and `ArchitectureDecisionRecordsTest` fails beyond. Then:

1. Move the entries to `docs/adr/index.md`, grouped by area.
2. Replace them in AGENTS.md by one line per area: `- <when the area applies>: read <area> in docs/adr/index.md`.
3. Adapt `ArchitectureDecisionRecordsTest` and this section.
