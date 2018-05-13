import {Selector} from "testcafe";
import {formatDateYMD} from '../DateUtil';

const ApplicationNew = function(t) {
    this.t = t;
};

ApplicationNew.prototype.from = function() {
    return this.t.click('#from')
};

ApplicationNew.prototype.findWorkingDay = async function() {
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
};

ApplicationNew.prototype.selectEmployee = function(name) {
   return this.t
       .typeText('#person-select', name);
};

ApplicationNew.prototype.from = function(date) {
    const dayOfApplication = formatDateYMD('{2}.{1}.{0}', date);
    return this.t
        .typeText('#from', dayOfApplication)
        .pressKey('tab');
};

ApplicationNew.prototype.to = function(date) {
    const dayOfApplication = formatDateYMD('{2}.{1}.{0}', date);
    return this.t
        .typeText('#to', dayOfApplication)
        .pressKey('tab');
};

ApplicationNew.prototype.submit = function() {
    return this.t.click('#submit');
};


module.exports = ApplicationNew;