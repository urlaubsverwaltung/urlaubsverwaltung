import { Selector } from 'testcafe';

const Login = function(t) {
    this.t = t;
};

Login.prototype.enterUser = async function(username) {
    return this.t.typeText('#username', username);
};

Login.prototype.enterPass = async function(password) {
    return this.t.typeText('#password', password);
};

Login.prototype.submit = async function() {
    return this.t.click('#submit');
};

Login.prototype.getErrorText = function() {
    return Selector('#login--error')().innerText;
};

Login.prototype.loginTestUser = async function() {
    await this.enterUser('testUser');
    await this.enterPass('secret');
    await this.submit();
};

module.exports = Login;