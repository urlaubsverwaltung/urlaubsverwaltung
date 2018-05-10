import { Selector } from 'testcafe';

export const selectors = Object.freeze({
  notification: Selector('.feedback'),
  // the first found box element is the userbox currently...
  userbox: Selector('.box'),
});

export async function ensureUserBox(t, { username, holidayType, date }) {
  await t
    .expect(selectors.userbox.innerText).eql(
      `${username} beantragt\n${holidayType} für ${date} , ganztägig\n`
    );
}

export default {
  selectors,
  ensureUserBox,
};
