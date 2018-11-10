import {Selector} from "testcafe";

class Application {

    constructor(t) {
        this.t = t;
    }

    async goToApplication(url) {
        await this.t.navigateTo(url);

        return this;
    }


    async clickCancel() {
        const cancelButton = Selector('i').withAttribute('class', 'fa fa-trash').parent();
        await this.t.click(cancelButton);

        return this;
    }

    async enterCancelMessage(msg) {
        await this.t.typeText('#text', msg);

        return this;
    }

    async clickCancelSubmit() {
        const cancelButton = Selector('button').withAttribute('class', 'btn btn-danger col-xs-12 col-sm-5');
        await this.t.click(cancelButton);

        return this;
    }

    async applicationIsCanceled() {
        await this.t.expect(Selector('tr').withAttribute('class', 'type-REVOKED').exists).ok();

        return this;
    }

    async isApplicationUrl() {
        // example location: /web/application/38
        const location = await this.t.eval(() => window.location);
        const pathParts = location.pathname.split('/');
        await this.t.expect(pathParts[1]).eql('web')
            .expect(pathParts[2]).eql('application')
            .expect(pathParts[3]).ok(`location: ${location.pathname} probably the day is already a vacation day`)
            .expect(Number.isInteger(Number(pathParts[3]))).ok(`location: ${location.pathname}`);

        return this;
    }

    async getApplicationUrl() {
        return await this.t.eval(() => window.location.href);
    }
}


module.exports = Application;