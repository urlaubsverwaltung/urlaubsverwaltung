import {Selector} from "testcafe";
import {formatDateYMD} from '../DateUtil';

const ApplicationNew = require('./applicationNew');

class Overview {
    constructor(t) {
        this.t = t;
    }

    async newApplication() {
    
        await this.t.click('#application_new');
    
        return new ApplicationNew(this.t);
    
    }

    async selectDay(date) {
        const day = formatDateYMD('{0}-{1}-{2}', date);
    
        const selectDate = Selector('span')
            .withAttribute('data-datepicker-date', day);
    
        return await this.t.click(selectDate);
    }
}


module.exports = Overview;