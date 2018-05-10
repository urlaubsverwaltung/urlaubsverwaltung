import addYears from 'date-fns/add_years'
import formatDate from 'date-fns/format';
import { doLogin } from '../pages/login';
import overview from '../pages/overview';
import holidayRequestForm from '../pages/holidayRequestForm';
import holidayRequestDetail from '../pages/holidayRequestDetail';

fixture `Login - Overview`
  .page `localhost:8080/`;
// .page `https://urlaubsverwaltung-demo.synyx.de/`;

const visibleDate = overview.calendarSelectors.date
  .with({ visibilityCheck: true });

test('request holiday', async t => {
  await doLogin(t, { username: 'testUser', password: 'secret' });

  // move calendar to next year
  await do12Times(() =>
    t.click(overview.calendarSelectors.next)
  );

  const todayInOneYear = formatDate(addYears(new Date(), 1), 'YYYY-MM-DD');
  const todayInOneYearFormatted = formatDate(addYears(new Date(), 1), 'DD.MM.YYYY');

  // verify visible month of next year
  const todayInOneYearElement = visibleDate(todayInOneYear);
  await t.expect(todayInOneYearElement.exists).ok();

  // TODO how to handle weekend or a feast day?
  // request holiday for this day of the next year
  await t.click(todayInOneYearElement);

  // afterwards the holiday request form must be visible
  // with pre-filled date values
  await holidayRequestForm.ensureFromInput(t, todayInOneYearFormatted);
  await holidayRequestForm.ensureToInput(t, todayInOneYearFormatted);
  await holidayRequestForm.submit(t);

  // afterwards the detail view must be visible
  await holidayRequestDetail.ensureUserBox(t, {
    username: 'Klaus Müller',
    holidayType: 'Erholungsurlaub',
    date: 'Fr, 10.05.2019',
  })
});

async function xTimes(max, command) {
  for (let i = 0; i < max; i++) {
    await command();
  }
}

const do12Times = command => xTimes(12, command);
