import { Selector } from 'testcafe';

export const selectors = Object.freeze({
  notification: Selector('.feedback'),
  // the first found box element is the userbox currently...
  userbox: Selector('.box'),
  approveBoxCommentElement: Selector('form#allow textarea'),
  approveBoxSubmitButton: Selector('form#allow [type="submit"]'),
});

export function ensureVisibility(t) {
  // TODO
  return t;
}

export async function ensureUserBox(t, { username, holidayType, date }) {
  return t.expect(selectors.userbox.innerText).eql(
    `${username} beantragt\n${holidayType} für ${date} , ganztägig\n`
  );
}

export function approve(t, { comment = '' }) {
  return t
    .typeText(selectors.approveBoxCommentElement, comment)
    .click(selectors.approveBoxSubmitButton);
}

export default {
  selectors,
  ensureVisibility,
  ensureUserBox,
  approve,
};
