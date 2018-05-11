import { Selector } from 'testcafe';

export const selectors = Object.freeze({
  fromDateInput: Selector('input#from'),
  toDateInput: Selector('input#to'),
  submitButton: Selector('form#application [type="submit"]'),
});

export function ensureFromInput(t, expectedValue) {
  return t.expect(selectors.fromDateInput.value).eql(expectedValue);
}

export function ensureToInput(t, expectedValue) {
  return t.expect(selectors.toDateInput.value).eql(expectedValue);
}

export function submit(t) {
  return t.click(selectors.submitButton);
}

export default {
  selectors,
  submit,
  ensureFromInput,
  ensureToInput,
};
