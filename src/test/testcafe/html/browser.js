
const Browser = function(t) {
    this.t = t;
};

Browser.prototype.getLocation = function() {
    return this.t.eval(() => window.location);
};

module.exports = Browser;