const {port} = require('../config');
const Login = require('../html/login');
const Browser = require('../html/browser');

fixture `Login.jsp`.page `http://localhost:${port}/login`;

test('wrong login', async t => {
    const login = new Login(t);

    login.enterUser('test');
    login.enterPass('test');
    login.submit();

    await t.expect(login.getErrorText()).eql('Der eingegebene Nutzername oder das Passwort ist falsch.');
});

test('correct login', async t => {
    const login = new Login(t);
    const browser = new Browser(t);

    login.enterUser('test');
    login.enterPass('secret');
    await login.submit();

    const location = await browser.getLocation();

    await t.expect(location.pathname).eql('/web/staff/4/overview');
});