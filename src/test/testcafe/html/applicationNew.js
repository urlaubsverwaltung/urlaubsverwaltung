import {Selector} from "testcafe";
import {formatDateYMD} from '../DateUtil';

const Application = require('./application');

class ApplicationNew {
    constructor(t) {
        this.t = t;
    }

    from() {
        return this.t.click('#from')
    }

    async findWorkingDay() {
        let day;

        await this.t.click('#from');

        let count = await Selector('td').count;

        // find first day in this month were a vacation application can be done
        for (let i=0; i<count; ++i) {
            let tdHandle = Selector('td').nth(i);
    
            let td = await tdHandle();
            const workday = td.classNames.indexOf('') > -1; // has an empty cssClass
            day = td.innerText.replace(/[^0-9]*/g, '');
            if (workday) {
                break;
            }
        }
        return day;
    }

    selectEmployee(name) {
        return this.t
            .typeText('#person-select', name);
    }

    async setFrom(date) {
        const dayOfApplication = formatDateYMD('{2}.{1}.{0}', date);
        await this.t
            .typeText('#from', dayOfApplication)
            .pressKey('tab');

        return this;
    }

    async setTo(date) {
        const dayOfApplication = formatDateYMD('{2}.{1}.{0}', date);
        await this.t
            .typeText('#to', dayOfApplication)
            .pressKey('tab');

        return this;
    }

    async submit() {
        await this.t.click('#submit');

        return new Application(this.t);
    }
}


module.exports = ApplicationNew;