import login, { doLogin, doSuccessfulLogin } from '../pages/login';
import navigation from '../pages/navigation'
import overview from '../pages/overview';

fixture `Login - Overview`
  .page `localhost:8080/`;
  // .page `https://urlaubsverwaltung-demo.synyx.de/`;

test('ensure login error message', async t => {
  await doLogin(t, { username: 'test', password: 'not-matching-password' });

  await t
    .expect(await login.selectors.errorBox.exists).ok()
    .expect(await login.selectors.errorBox.innerText).eql(
      'Der eingegebene Nutzername oder das Passwort ist falsch.'
    );
});

test('ensure overview for current year after successful login', async t => {
  await doLogin(t, { username: 'test', password: 'secret' });

  // ensure visible navigation
  //
  await navigation.ensureVisiblity(t);

  // ensure some overview elements
  //
  await overview.ensureYearPicker(t, {
    year: String(new Date().getFullYear()),
  });
  await overview.ensureUserBox(t, {
    username: 'Marlene Muster',
    email: 'office@firma.test',
  });
});
