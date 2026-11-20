# CSS conventions

Apply them to CSS you add or change in `src/main/css` and `src/main/javascript`. Leave other code as it is.

- Do not use `@apply`. Write the native declaration with the theme variable of Tailwind, e.g.
  `font-weight: var(--font-weight-bold);`, `border-radius: var(--radius-sm);`,
  `border-color: var(--color-gray-200);` or `padding: --spacing(4);`.
- A font size is the declaration `font-size: var(--text-sm);`. Add `line-height: var(--text-sm--line-height);` when
  the element should not keep the line height it has, e.g. the one a heading gets from the base styles.
- Use `@variant` for breakpoints and states, e.g. `@variant md`, `@variant dark` or `@variant hover`.
- A utility without a single native declaration, e.g. `sr-only`, stays a class in the template.
- A bundle with `@reference` only gets the theme variables that `style.css` contains, and Tailwind only adds those in
  use. After `npm run css` check that a variable you use for the first time is defined in
  `target/classes/static/css/style.css`.
