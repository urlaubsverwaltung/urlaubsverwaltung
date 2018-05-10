import { Selector } from 'testcafe';

export const selectors = Object.freeze({
  fromDateInput: Selector('input#from'),
  toDateInput: Selector('input#to'),
  submitButton: Selector('form#application [type="submit"]'),
});

export async function ensureFromInput(t, expectedValue) {
  await t
    .expect(selectors.fromDateInput.value).eql(expectedValue);
}

export async function ensureToInput(t, expectedValue) {
  await t
    .expect(selectors.toDateInput.value).eql(expectedValue);
}

export async function submit(t) {
  await t.click(selectors.submitButton);
}

export default {
  selectors,
  submit,
  ensureFromInput,
  ensureToInput,
};
