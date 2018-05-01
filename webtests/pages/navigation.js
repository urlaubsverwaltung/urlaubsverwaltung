import { Selector } from 'testcafe';

export const selectors = Object.freeze({
  me: Selector('a[href="/web/overview"]'),
  logout: Selector('a[href="/logout"]'),
  create: Selector('a[href="/web/application/new"]'),
  settings: Selector('a[href="/web/settings"]'),
});

export function ensureVisiblity(t) {
  // note: logout text starts with a space oO
  return t.expect(selectors.logout.innerText).eql(' Logout');
}

export default {
  selectors,
  ensureVisiblity,
};
