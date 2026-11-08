---
status: accepted
date: 2026-11-08
author: Benjamin Seber
---

# Prefer native HTML and visible text over ARIA

## Decision

We will use native HTML elements and attributes and visible or visually hidden text instead of ARIA, e.g.
`required` instead of `aria-required`, `<button>` instead of `role="button"`, a `<span class="sr-only">` instead of
`aria-label`. ARIA is used only where HTML has no equivalent, e.g. `aria-expanded` or `aria-controls`.

## Context

- The principle was first recorded as part of
  [Mark required form fields with the required attribute …](2026-10-30-accessible-required-form-fields.md), but
  applies to every template and script, not only to forms.
- Native elements and attributes come with semantics and behaviour: focus, keyboard handling, form participation.
  ARIA only changes what assistive technology announces, the behaviour has to be scripted separately.
- Text in the document is localised with the page and visible to every user and tool. `aria-label` is announced
  only, and is easily left out, e.g. by translation tools.
- The W3C's first rule of ARIA use: if a native HTML element or attribute with the needed semantics and behaviour
  exists, use it instead of repurposing an element and adding ARIA.
- Decision criteria:
  - Assistive technology announces elements correctly.
  - Elements behave as expected without additional scripting.
  - Labels are localised like any other text.

## Options

1. (SELECTED) Native HTML first: ARIA only where HTML has no equivalent.
2. ARIA where convenient: ARIA attributes and roles are used freely, e.g. on generic elements.
3. Do nothing: no rule, decided case by case.

## Consequences

**Option 1 (SELECTED): Native HTML first**

- Selected because native elements bring their behaviour along, and text in the document is localised with it.
- Selected despite needing to know where HTML has no equivalent, which is not always obvious.
- Selected despite visually hidden text needing the `sr-only` class and adding markup.
- Selected despite existing uses of ARIA, e.g. `aria-label`, not having been checked for a native equivalent.

**Option 2: ARIA where convenient**

- Rejected because the behaviour of native elements has to be rebuilt by scripts, and mistakes there are easy to make
  and hard to notice without assistive technology.

**Option 3: Do nothing**

- Rejected because the choice would be made again for every element, with inconsistent results.

## Advice

Not recorded.
