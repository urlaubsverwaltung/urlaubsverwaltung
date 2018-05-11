import { ClientFunction } from 'testcafe';
import addYears from 'date-fns/add_years'
import formatDate from 'date-fns/format';
import { doLogin } from '../pages/login';
import { doLogout } from '../pages/navigation';
import overview from '../pages/overview';
import holidayRequestForm from '../pages/holidayRequestForm';
import holidayRequestDetail from '../pages/holidayRequestDetail';
import holidayOverview from '../pages/holidayOverview';

fixture `Login - Overview`
  .page `localhost:8080/`;
// .page `https://urlaubsverwaltung-demo.synyx.de/`;

const visibleDate = overview.calendarSelectors.date
  .with({ visibilityCheck: true });

test.only('request holiday and accept it', async t => {
  const todayInOneYear = formatDate(addYears(new Date(), 1), 'YYYY-MM-DD');
  const todayInOneYearFormatted = formatDate(addYears(new Date(), 1), 'DD.MM.YYYY');
  const todayInOneYearElement = visibleDate(todayInOneYear);
  const getHolidayIdFromUrl = ClientFunction(() => window.location.pathname.split('/').pop(-1));

  await doLogin(t, { username: 'testUser', password: 'secret' });

  // move calendar to next year
  await do12Times(() =>
    t.click(overview.calendarSelectors.next)
  );

  // verify visible month of next year
  await t.expect(todayInOneYearElement.exists).ok();

  // TODO how to handle weekend or a feast day?
  // request holiday for this day of the next year
  await t.click(todayInOneYearElement);

  // afterwards the holiday request form must be visible
  // with pre-filled date values
  await holidayRequestForm.ensureFromInput(t, todayInOneYearFormatted);
  await holidayRequestForm.ensureToInput(t, todayInOneYearFormatted);
  await holidayRequestForm.submit(t);

  const holidayEntryId = await getHolidayIdFromUrl();

  // afterwards the detail view must be visible
  await holidayRequestDetail.ensureUserBox(t, {
    username: 'Klaus Müller',
    holidayType: 'Erholungsurlaub',
    date: 'Fr, 10.05.2019',
  });

  await doLogout(t);

  // login as user who is allowed to approve the holiday entry
  await doLogin(t, { username: 'test', password: 'secret' });

  await holidayOverview.open(t);
  await holidayOverview.clickApproveHolidayEntry(t, { holidayEntryId });

  await holidayRequestDetail.ensureVisibility(t);
  await holidayRequestDetail.approve(t, { comment: 'have fun!' });

  await holidayOverview.ensureVisibility(t);
  await holidayOverview.ensureApprovedHolidayEntry(t, { holidayEntryId });
});

async function xTimes(max, command) {
  for (let i = 0; i < max; i++) {
    await command();
  }
}

const do12Times = command => xTimes(12, command);
