const {port} = require('../config');
const Browser = require('../html/browser');
const Application = require('../html/application');

fixture`applicaitons`
    .page`http://localhost:${port}/login`;

const today = new Date();
const month = today.getMonth();
const year = today.getFullYear();
let applicationUrl;


test('new application as a user', async t => {
    const browser = new Browser(t);

    let applicationNew = await browser.maximizeWindow()
        .then(login => login.loginTestUser())
        .then(overview => overview.newApplication());

    const day = await applicationNew.findWorkingDay();

    const dateOfApplication = new Date(year, month, day);

    applicationUrl = await applicationNew
        .setFrom(dateOfApplication)
        .then(applicationNew => applicationNew.setTo(dateOfApplication))
        .then(applicationNew => applicationNew.submit())
        .then(application => application.isApplicationUrl())
        .then(application => application.getApplicationUrl());
});


test('cancel application as a user', async t => {
    const browser = new Browser(t);

    await browser.maximizeWindow()
        .then(login => login.loginTestUser());


    await new Application(t).goToApplication(applicationUrl)
        .then(application => application.clickCancel())
        .then(application => application.enterCancelMessage('I do not like vacations'))
        .then(application => application.clickCancelSubmit())
        .then(application => application.applicationIsCanceled());

});

