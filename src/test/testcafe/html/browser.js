
const Browser = function(t) {
    this.t = t;
};

Browser.prototype.getLocation = function() {
    return this.t.eval(() => window.location);
};


Browser.prototype.maximizeWindow = function() {
    return this.t.maximizeWindow();
};

module.exports = Browser;