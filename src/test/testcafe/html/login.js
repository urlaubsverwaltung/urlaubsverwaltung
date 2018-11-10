import { Selector, Role } from 'testcafe';
const Overview = require('./overview');

const {port} = require('../config');

class Login {
    constructor(t) {
        this.t = t;
    }

    static users = {};

    static role(usernmae) {
        if (Login.users[usernmae] === undefined) {
            Login.users[usernmae] = Role(`http://localhost:${port}/login`, async t => {
                await t
                    .typeText('#username', usernmae)
                    .typeText('#password', 'secret')
                    .click('#submit');
            });
        }
        return Login.users[usernmae];
    }

    async enterUser(username) {
        return this.t.typeText('#username', username);
    }

    async enterPass(password) {
        return this.t.typeText('#password', password);
    }

    async submit() {
        return this.t.click('#submit');
    }

    static getErrorText() {
        return Selector('#login--error')().innerText;
    }

    async loginAsUser() {

        return this.loginAs('testUser');
    }

    async loginAsOffice() {

        return this.loginAs('test');
    }


    async loginAs(usernmae) {
        await this.t.useRole(Login.role(usernmae))
            .navigateTo(`http://localhost:${port}`);

        return new Overview(this.t);
    }
}

module.exports = Login;