import { Selector } from 'testcafe';

export const selectors = Object.freeze({
  username: Selector('#username'),
  password: Selector('#password'),
  submit: Selector('[type="submit"]'),
  errorBox: Selector('#login--error'),
});

export function doLogin(t, { username, password }) {
  return t
    .typeText(selectors.username, username)
    .typeText(selectors.password, password)
    .click(selectors.submit);
}

export default {
  doLogin,
  selectors,
};
