const Login = require('./login');

const Browser = function(t) {
    this.t = t;
};

Browser.prototype.getLocation = function() {
    return this.t.eval(() => window.location);
};

Browser.prototype.maximizeWindow = async function () {

    await this.t.maximizeWindow();

    return new Login(this.t);
};

module.exports = Browser;