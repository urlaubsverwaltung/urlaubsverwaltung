import {Selector} from "testcafe";
import {formatDateYMD} from '../DateUtil';

const Overview = function(t) {
    this.t = t;
};

Overview.prototype.newApplication = function() {
    return this.t.click('#application_new');
};

Overview.prototype.selectDay = async function(date) {
    const day = formatDateYMD('{0}-{1}-{2}', date);

    const selectDate = Selector('span')
        .withAttribute('data-datepicker-date', day);

    return await this.t.click(selectDate);
};


module.exports = Overview;