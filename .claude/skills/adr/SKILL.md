---
name: adr
description: Write and maintain the Architecture Decision Records (ADRs) of Urlaubsverwaltung in docs/adr — start a new decision, propose an option, collect advice, accept, supersede, expire or retire an ADR, and keep the "Decisions" index in AGENTS.md in sync. Use when the user wants to record, propose, accept or replace a decision, discusses an ADR, or asks "what did we decide about X". Also when a discussion settles a decision that affects more than one feature or is hard to reverse: suggest recording it as an ADR.
---

# ADR

`docs/adr/README.md` defines file names, sections, frontmatter, lifecycle and the index. **Read it first** and follow
it; `docs/adr/template.md` is the starting point for a new ADR. `docs/adr/2026-11-08-prefer-native-html-over-aria.md`
is a short, complete example to model a new ADR on. This skill describes how to work through each step, it does not
repeat or change those conventions.

Find out which of the tasks below the user wants. Ask if it is not clear.

## Suggest an ADR

When a discussion settles a decision that affects more than one feature or is hard to reverse, and nobody asked for an
ADR: suggest recording it in one sentence, naming the decision. Start "New ADR" only when the user agrees. Smaller
decisions need no ADR.

## Ground rules

- **People decide, you draft.** Do not select an option, accept an ADR or write advice nobody gave. Suggest, and let
  the author confirm.
- Dates are today in `YYYY-MM-DD`. The author defaults to `git config user.name`; confirm it.

## New ADR

1. **State the decision to be made** in one sentence, together with the user.
2. **Look for related ADRs** in `docs/adr/[0-9]*.md`. Name the ones the new ADR may supersede or contradict.
3. **Create** the ADR from the template as `draft`, with a working title.
4. **Context:** ask what makes the decision necessary now, which constraints apply and which criteria the options
   are measured against. Check facts in the code instead of asking for them. Bullets, no prose.
5. **Options:** add options the user did not think of and question the favourite. One line each.
6. **Consequences:** measure each option against the criteria from the Context. For a rejected option its decisive
   drawback is enough. The reasons are the author's: mark the ones you add yourself, e.g. `(suggested)`, and let the
   author confirm or drop them before the ADR is proposed.
7. **Whom to ask for advice:** people affected by the decision and people who know the topic. Recent contributors to
   the affected code are good candidates:

   ```bash
   git shortlog -sn --since="1 year ago" HEAD -- <paths> | grep -v '\[bot\]' | head -5
   ```

   One person may appear under several names (e.g. full name and GitHub login). Write them under `Asked:` once the
   user agrees.

## Propose an option

When the author selects an option for now:

1. Move it to the top with `(SELECTED)` and renumber the other options and their Consequences.
2. Write the **Decision** section and the **title** for the selected option.
3. Set the status to `proposed`.
4. Add the index entry to `AGENTS.md`.
5. Suggest a pull request for the ADR.

## Collect advice

1. Take the advice from the user or from the pull request of the ADR:

   ```bash
   # conversation
   gh api --paginate repos/urlaubsverwaltung/urlaubsverwaltung/issues/<number>/comments --jq '.[] | "\(.user.login) \(.created_at[:10]): \(.body)"'
   # review summaries (approve, request changes, comment)
   gh api --paginate repos/urlaubsverwaltung/urlaubsverwaltung/pulls/<number>/reviews --jq '.[] | select(.body != "") | "\(.user.login) \(.submitted_at[:10]) \(.state): \(.body)"'
   # inline comments on lines of the ADR
   gh api --paginate repos/urlaubsverwaltung/urlaubsverwaltung/pulls/<number>/comments --jq '.[] | "\(.user.login) \(.created_at[:10]) line \(.line // .original_line): \(.body)"'
   ```

2. Add the advice on the decision as given to the Advice section, in the format of the template: on its context,
   options or consequences, not on wording or formatting of the ADR. Ask when unsure. Ask for the role when it is not
   known. Do not turn criticism into agreement.
3. Point out advice that Context, Options or Consequences do not reflect yet, and offer to work it in.

## Accept

Only when the author says the decision is taken. Check first and name what is missing:

- Everyone under `Asked:` gave advice, or the author accepts that someone did not.
- The ADR meets the README's Sections, notably for Options and for advice that was not followed.

Then:

1. Bring the **Decision** section and the **title** up to date with the advice.
2. Set the status to `accepted` and update the index entry.
3. Suggest a test where a rule of the decision can be checked automatically.
4. If the ADR replaces others, supersede them (see below).

## Supersede, extract, expire, retire

- **Supersede:** write the new decision as a new ADR (see "New ADR"), then set the old one to `superseded`.
- **Extract:** write the part as a new ADR (see "New ADR") and replace it in the old one by a link. Check that the old
  ADR's Decision still stands on its own and its index entry still fits.
- **Expire:** set the status to `expired` and suggest a new ADR to revisit the decision.
- **Retire:** set the status to `retired`.

Update the index in `AGENTS.md` as the README's Index section says.

## Check the index

After every change of status, title or file name run:

```bash
./mvnw test -Dskip.npm -Dskip.installnodenpm -Dtest=ArchitectureDecisionRecordsTest
```

It fails when an entry in `AGENTS.md` is missing, outdated or left over, and when the index outgrows `AGENTS.md`.

## Answer "what did we decide about X"

Search titles and content of `docs/adr/[0-9]*.md`. Read the Decision section of the matching ones first
(`sed '/^## Context/q' <file>`), the rest only when the question is about why. Name accepted ADRs first, superseded
ones only together with the ADR that replaced them, and say clearly when an ADR is only `draft` or `proposed`.
