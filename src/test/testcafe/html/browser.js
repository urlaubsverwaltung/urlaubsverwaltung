const Login = require('./login');

class Browser {
    constructor(t) {
        this.t = t;
    }

    getLocation() {
        return this.t.eval(() => window.location);
    }

    async maximizeWindow() {
    
        await this.t.maximizeWindow();
    
        return new Login(this.t);
    }
}

module.exports = Browser;