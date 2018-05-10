import { Selector } from 'testcafe';

fixture `overview.jsp`
    .page `http://localhost:8080/login`;

function formatDate(date) {
    const day = date.getDate();
    const month = date.getMonth() + 1;
    const year = date.getFullYear();

    return `${day}.${month}.${year}`;
}

test('new application as user', async t => {
    const dayOfApplication = formatDate(new Date(2018,0,3));

    await t
        .maximizeWindow() // otherwise not all buttons are visible
        .typeText('#username', 'testUser')
        .typeText('#password', 'secret')
        .click('#submit')
        .click('#application_new')
        .typeText('#from', dayOfApplication)
        .pressKey('tab')
        .typeText('#to', dayOfApplication)
        .pressKey('tab')
        .click('#submit');

    const location = await t.eval(() => window.location);
    // example location: /web/application/38
    const pathParts = location.pathname.split('/');

    await t.expect(pathParts[1]).eql('web')
        .expect(pathParts[2]).eql('application')
        .expect(pathParts[3]).ok(`location: ${location.pathname} probably the day is already a vacation day`)
        .expect(Number.isInteger(Number(pathParts[3]))).ok(`location: ${location.pathname}`);

});

test('remove my application as user', async t => {
    const dayOfApplication = formatDate(new Date());

    await t
        .maximizeWindow() // otherwise not all buttons are visible
        .typeText('#username', 'testUser')
        .typeText('#password', 'secret')
        .click('#submit');


    const today = Selector('span').withAttribute('data-datepicker-date', '2018-04-20');
    await t.click(today);

    const rmVacation = Selector('i').withAttribute('class', 'fa fa-trash');
    await t.click(rmVacation);

    const stornButton = Selector('button').withAttribute('class', 'btn btn-danger col-xs-12 col-sm-5');
    await t.click(stornButton);
    /*
    const location = await t.eval(() => window.location);
    // example location: /web/application/38
    const pathParts = location.pathname.split('/');

    await t.expect(pathParts[1]).eql('web')
        .expect(pathParts[2]).eql('application')
        .expect(pathParts[3]).ok(`location: ${location.pathname}`)
        .expect(Number.isInteger(Number(pathParts[3]))).ok(`location: ${location.pathname}`);
*/
});