
const format = (formatStr, ...args) => {
    return formatStr.replace(/\{(\d+)\}/g, (m, i) => {
        return args[i];
    })
};

function formatDateYMD(formatStr, date) {
    const day = date.getDate();
    const month = date.getMonth() + 1;
    const year = date.getFullYear();

    let month2digits = month;
    if (month < 10) {
        month2digits = `0${month}`;
    }

    let day2digits = day;
    if (day < 10) {
        day2digits = `0${day}`;
    }

    return format(formatStr, year, month2digits, day2digits);
}

module.exports = {
    formatDateYMD,

};