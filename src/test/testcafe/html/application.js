import {Selector} from "testcafe";

const Application = function(t, url) {
    this.t = t;
};

Application.prototype.goToApplication = async function(url) {
    await this.t.navigateTo(url);

    return this;
};


Application.prototype.clickCancel = async function() {
    const cancelButton = Selector('i').withAttribute('class', 'fa fa-trash').parent();
    await this.t.click(cancelButton);

    return this;
};

Application.prototype.enterCancelMessage = async function(msg) {
    await this.t.typeText('#text', msg);

    return this;
};

Application.prototype.clickCancelSubmit = async function() {
    const cancelButton = Selector('button').withAttribute('class', 'btn btn-danger col-xs-12 col-sm-5');
    await this.t.click(cancelButton);

    return this;
};

Application.prototype.applicationIsCanceled = async function() {
    await this.t.expect(Selector('tr').withAttribute('class', 'type-REVOKED').exists).ok();

    return this;
};

Application.prototype.isApplicationUrl = async function() {
    // example location: /web/application/38
    const location = await this.t.eval(() => window.location);
    const pathParts = location.pathname.split('/');
    await this.t.expect(pathParts[1]).eql('web')
        .expect(pathParts[2]).eql('application')
        .expect(pathParts[3]).ok(`location: ${location.pathname} probably the day is already a vacation day`)
        .expect(Number.isInteger(Number(pathParts[3]))).ok(`location: ${location.pathname}`);

    return this;
};

Application.prototype.getApplicationUrl = async function() {
    return await this.t.eval(() => window.location.href);
};


module.exports = Application;