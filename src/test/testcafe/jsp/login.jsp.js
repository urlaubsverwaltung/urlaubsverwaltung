import { Selector } from 'testcafe'; // first import testcafe selectors

fixture `Login.jsp`// declare the fixture
    .page `http://localhost:8080/login`;  // specify the start page


//then create a test and place your code there
test('wrong login', async t => {
    await t
        .typeText('#username', 'test')
        .typeText('#password', 'test')
        .click('#submit')
        // Use the assertion to check if the actual header text is equal to the expected one
        .expect(Selector('#login--error').innerText).eql('Der eingegebene Nutzername oder das Passwort ist falsch.');
});

test('correct login', async t => {
    await t
        .typeText('#username', 'test')
        .typeText('#password', 'secret')
        .click('#submit');
    // Use the assertion to check if the actual header text is equal to the expected one
    //.expect(Selector('#login--error').innerText).eql('Der eingegebene Nutzername oder das Passwort ist falsch.');

    const location = await t.eval(() => window.location);

    await t.expect(location.pathname).eql('/web/staff/4/overview');
});