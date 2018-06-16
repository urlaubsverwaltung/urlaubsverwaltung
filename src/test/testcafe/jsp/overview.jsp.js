const {port} = require('../config');
import { Selector } from 'testcafe';

fixture `overview.jsp`
    .page `http://localhost:${port}/login`
    .beforeEach(async t => {
        await t
            .maximizeWindow() // otherwise not all buttons are visible
            .typeText('#username', 'test')
            .typeText('#password', 'secret')
            .click('#submit');
    });

test('new application', async t => {
    await t.click('#application_new');
    const location = await t.eval(() => window.location);

    await t.expect(location.pathname).eql('/web/application/new');
});

[
    {msg:'Menu: click on new application link',
        idToClick:'#application_new', expectedLocation:'/web/application/new'},
    {msg:'Menu: click on menu new application link',
        idToClick:'#menu_new', expectedLocation:'/web/application/new'},
    {msg:'Menu: click on menu overview link',
        idToClick:'#menu_overview', expectedLocation:'/web/staff/4/overview'},
    {msg:'Menu: click on menu application link',
        idToClick:'#menu_application', expectedLocation:'/web/application'},
    {msg:'Menu: click on menu sicknote link',
        idToClick:'#menu_sicknote', expectedLocation:'/web/sicknote/'},
    {msg:'Menu: click on menu staff link',
        idToClick:'#menu_staff', expectedLocation:'/web/staff'},
    {msg:'Menu: click on menu department link',
        idToClick:'#menu_department', expectedLocation:'/web/department'},
    {msg:'Menu: click on menu settings link',
        idToClick:'#menu_settings', expectedLocation:'/web/settings'},
    {msg:'Menu: click on menu logout link',
        idToClick:'#menu_logout', expectedLocation:'/login'}
].forEach(data => test(data.msg, async t => {

    await t.click(data.idToClick);
    const location = await t.eval(() => window.location);

    await t.expect(location.pathname).eql(data.expectedLocation);
}));