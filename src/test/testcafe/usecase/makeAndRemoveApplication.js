import { Selector } from 'testcafe';

fixture `overview.jsp`
    .page `http://localhost:8080/login`;

const today = new Date();
const month = today.getMonth();
const year = today.getFullYear();
let day;

const format = (formatStr, ...args) => {
    return formatStr.replace(/\{(\d+)\}/g, (m, i) => {
        return args[i];
    })
};

function formatDateYMD(formatStr, date) {
    const day = date.getDate();
    const month = date.getMonth() + 1;
    const year = date.getFullYear();

    return format(formatStr, year, month, day);
}

test.only('new application as user', async t => {

    await t
        .maximizeWindow() // otherwise not all buttons are visible
        .typeText('#username', 'testUser')
        .typeText('#password', 'secret')
        .click('#submit')
        .click('#application_new')
        .click('#from');

    let count = await Selector('td').count;

    // find first day in this month were a vacation application can be done
    for (let i=0; i<count; ++i) {
        let tdHandle = Selector('td').nth(i);

        let td = await tdHandle();
        const workday = td.classNames.indexOf('') > -1; // has an empty cssClass
        day = td.innerText.replace(/[^0-9]*/g, '');
        if (workday) {
            break;
        }
    }

    const dayOfApplication = formatDateYMD('{2}.{1}.{0}', new Date(year,month,day));

    await t
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

test.only('remove my application as user', async t => {
    const dayOfApplicationDatePicker = formatDateYMD('{0}-0{1}-0{2}', new Date(year,month,day));

    await t
        .maximizeWindow() // otherwise not all buttons are visible
        .typeText('#username', 'testUser')
        .typeText('#password', 'secret')
        .click('#submit');

    let headerText = await Selector('.datepicker-month').find('h3').nth(3).textContent;

    const dayToRemove = Selector('span').withAttribute('data-datepicker-date', dayOfApplicationDatePicker);
    await t.click(dayToRemove);

    const rmVacation = Selector('i').withAttribute('class', 'fa fa-trash');
    await t.click(rmVacation);

    const stornButton = Selector('button').withAttribute('class', 'btn btn-danger col-xs-12 col-sm-5');
    await t.click(stornButton);

    const location = await t.eval(() => window.location);
    // example location: /web/application/38
    const pathParts = location.pathname.split('/');

    await t.expect(pathParts[1]).eql('web')
        .expect(pathParts[2]).eql('application')
        .expect(pathParts[3]).ok(`location: ${location.pathname}`)
        .expect(Number.isInteger(Number(pathParts[3]))).ok(`location: ${location.pathname}`);
});