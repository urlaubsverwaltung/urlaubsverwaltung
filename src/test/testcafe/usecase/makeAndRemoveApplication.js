const Browser = require('../html/browser');
const Login = require('../html/login');
const Overview = require('../html/overview');
const ApplicationNew = require('../html/applicationNew');
const Application = require('../html/application');


fixture `overview.jsp`
    .page `http://localhost:${process.env.serverPort||8080}/login`;

const today = new Date();
const month = today.getMonth();
const year = today.getFullYear();
let day;

test('new application as user', async t => {
    const browser = new Browser(t);
    const login = new Login(t);
    const overview = new Overview(t);
    const applicationNew = new ApplicationNew(t);
    const application = new Application(t);

    await browser.maximizeWindow();
    await login.loginTestUser();
    await overview.newApplication();

    day = await applicationNew.findWorkingDay();

    const dateOfApplication = new Date(year, month, day);

    await applicationNew.from(dateOfApplication);
    await applicationNew.to(dateOfApplication);
    await applicationNew.submit();

    const location = await browser.getLocation();

    await application.isApplicationUrl(location);
});

test('remove my application as user', async t => {
    const browser = new Browser(t);
    const login = new Login(t);
    const overview = new Overview(t);
    const application = new Application(t);

    await browser.maximizeWindow();
    await login.loginTestUser();

    await overview.selectDay(new Date(year, month, day));

    await application.clickDelete();
    await application.clickStorn();

    const location = await browser.getLocation();

    await application.isApplicationUrl(location);
});