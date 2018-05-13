import {Selector} from "testcafe";

const Application = function(t) {
    this.t = t;
};

Application.prototype.clickDelete = function() {
    const rmVacation = Selector('i').withAttribute('class', 'fa fa-trash');
    return this.t.click(rmVacation);
};

Application.prototype.clickStorn = function() {
    const stornButton = Selector('button').withAttribute('class', 'btn btn-danger col-xs-12 col-sm-5');
    return this.t.click(stornButton);
};

Application.prototype.isApplicationUrl = function(location) {
    // example location: /web/application/38
    const pathParts = location.pathname.split('/');
    return this.t.expect(pathParts[1]).eql('web')
        .expect(pathParts[2]).eql('application')
        .expect(pathParts[3]).ok(`location: ${location.pathname} probably the day is already a vacation day`)
        .expect(Number.isInteger(Number(pathParts[3]))).ok(`location: ${location.pathname}`);
};


module.exports = Application;