import { Selector } from 'testcafe';
const Overview = require('./overview');


class Login {
    constructor(t) {
        this.t = t;
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

    async loginTestUser() {

        await this.enterUser('testUser');
        await this.enterPass('secret');
        await this.submit();

        return new Overview(this.t);
    }
}

module.exports = Login;