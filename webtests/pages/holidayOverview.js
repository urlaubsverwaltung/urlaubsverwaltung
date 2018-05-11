import { Selector } from 'testcafe';
import navigation from './navigation';

export const selectors = Object.freeze({
  title: Selector('legend'),
  holidayEntryForId: Selector(entryId =>
    document.querySelector(`[onClick="navigate('/web/application/${entryId}');"]`)
  ),
  feedbackBox: Selector('.feedback'),
});

export function open(t) {
  return navigation.goToHolidayOverview(t);
}

export function ensureVisibility(t) {
  const visibleTitleElement = selectors.title.with({ checkVisibility: true });
  return t
    .expect(visibleTitleElement.exists).ok()
    .expect(visibleTitleElement.textContent).contains('Offene Urlaubsanträge');
}

export async function clickApproveHolidayEntry(t, { holidayEntryId }) {

  // const f = new Function(
  //   `return document.querySelector('[onClick="navigate(\\'/web/application/${holidayEntryId}\\');"] .fa-action.positive')`
  // );

  return t.click(
    // erster versuch: (andere versuche gestartet weil holidayEntryId undefined war die ganze zeit >.<)
    selectors.holidayEntryForId(holidayEntryId).find('.fa-action.positive')
    // 2: selectors.holidayEntryForId(holidayEntryId).find(e => {
    //   console.log(e);
    //   return e.classList.contains('positive');
    // })
    // 3: Selector(() =>
    //   // note: cannot access closure 'holidayEntryId'
    //   //       selector function is serialized an send to $$ via postMessage
    //   // document.querySelector(`[onClick="navigate('/web/application/${holidayEntryId}');"] .fa-action.positive`)
    // )
    // Selector(f)
  );
}

export function ensureApprovedHolidayEntry(t, { holidayEntryId }) {
  const visibleFeedbackBox = selectors.feedbackBox.with({ checkVisibility: true });
  const visibleHolidayEntry = selectors.holidayEntryForId.with({ checkVisibility: true })(holidayEntryId) ;
  return t
    .expect(visibleFeedbackBox.exists).ok()
    .expect(visibleFeedbackBox.textContent).match(/Der Urlaubsantrag wurde genehmigt./)
    .expect(visibleHolidayEntry.exists).notOk();
}

export default {
  selectors,
  open,
  ensureVisibility,
  ensureApprovedHolidayEntry,
  clickApproveHolidayEntry,
};
