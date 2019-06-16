/* eslint-disable @urlaubsverwaltung/no-date-fns,unicorn/filename-case */
import { getTime, getDate, getMonth, getYear, startOfMonth, startOfWeek, format, addDays, addWeeks, addMonths, subMonths, isWeekend, isToday, isSameDay } from 'date-fns';

const element = (tagName, attributesOrChildren, children) => {
  const [tagWithId, ...classNames] = tagName.split('.');
  const [tag, id] = tagWithId.split('#');
  const element = document.createElement(tag);
  if (id) {
    element.id = id;
  }
  element.classList.add(...classNames);
  if (typeof attributesOrChildren === 'string') {
    element.textContent = attributesOrChildren;
  } else if (Array.isArray(attributesOrChildren)) {
    element.append(...attributesOrChildren);
  } else if (attributesOrChildren) {
    for (const [key, value] of Object.entries(attributesOrChildren)) {
      element.setAttribute(key, value);
    }
  }
  if (Array.isArray(children)) {
    element.append(...children);
  } else if (typeof children === 'string') {
    element.textContent = children;
  }
  return element;
};

class Datepicker extends HTMLElement {
  constructor() {
    super();

    // get all style rules for bootstraps 'form-control' class
    //Array.from(document.styleSheets.entries()).reduce((target, source) => ([...target, ...source]), []).filter(element => !Number.isInteger(element)).filter(stylesheet => Array.from(stylesheet.cssRules).some(rule => (rule.selectorText || '').includes('form-control'))).reduce((target, source) => ([...target, ...(Array.from(source.cssRules).filter(rule => (rule.selectorText || '').includes('form-control')))]), [])

    const style = element('style');
    style.innerHTML = `
      :host {
        display: block;
        position: relative;
      }
      .search-input {
        /* stuff currently defined by UV on an input */
        font: var(--input-font);
        line-height: var(--input-line-height);
        color: var(--input-color);
        margin: var(--input-margin);
        box-sizing: var(--input-box-sizing);
        
        /* text is only used for the 'clear' button feature */
        /* hide text */
        color: transparent;
        /* and hide that this is a text field */
        cursor: default;
        
        /* render search input like a text input */
        /* (e. g. with a real edge instead of round corners) */
        -webkit-appearance: textfield;
      }
      
      .flex {
        display: flex;
      }
      .flex-row {
        flex-direction: row;
      }
      .flex-1 {
        flex: 1 0 0;
      }
      .items-center {
        align-items: center;
      }
      .text-center {
        text-align: center;
      }
      .font-semibold: {
        font-weight: 600;
      }
      .p-2 {
        padding: 0.5rem;
      }
      .px-1 {
        padding-left: 0.25rem;
        padding-right: 0.25rem;
      }
      .px-2 {
        padding-left: 0.5rem;
        padding-right: 0.5rem;
      }
      .mb-4 {
        margin-bottom: 1rem;
      }
      .shadow {
        box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
      }
      .absolute {
        position: absolute;
      }
      .z-10 {
        z-index: 10;
      }
      .absolute {
        position: absolute;
      }
      .w-full {
        width: 100%;
      }
      .h-full {
        height: 100%;
      }
      .top-0 {
        top: 0;
      }
      .left-0 {
        left: 0;
      }
      .select-none {
        user-select: none;
      }
      
      span:focus {
        outline: none;
        background-color: Highlight;
      }
      
      .focused {
        outline-width: 2px;
        outline-style: solid;
        outline-color: Highlight;
      
        /* style matches OSX firefox */
        /* outline: 1px dotted auto; */
        /* style matches OSX chrome */
        outline: auto 5px -webkit-focus-ring-color;
        outline-offset: -2px;
      }
    `;

    const datepickerFacadeElement = element('input#my-datepicker', {
      type: 'hidden',
      name: 'my-datepicker',
    });
    const datepickerInputElement = element('input.search-input', {
      type: 'search',
      // date / month / year placeholder elements are focusable
      tabindex: '-1',
    });

    const dayElement = element('span', { tabindex: '0' }, 'dd');
    const daySeparator = element('span', '.');
    const monthElement = element('span', { tabindex: '0' }, 'mm');
    const monthSeparator = element('span', '.');
    const yearElement = element('span', { tabindex: '0' }, 'yyyy');

    const placeholder = element('div');
    placeholder.classList.add('absolute', 'top-0', 'left-0', 'h-full', 'flex', 'items-center', 'px-2', 'cursor-default', 'select-none');
    placeholder.append(dayElement, daySeparator, monthElement, monthSeparator, yearElement);

    this.datepickerFacadeElement = datepickerFacadeElement;
    this.datepickerInputElement = datepickerInputElement;
    this.dayPlaceholderElement = dayElement;
    this.monthPlaceholderElement = monthElement;
    this.yearPlaceholderElement = yearElement;
    this.datepickerPlaceholder = placeholder;


    let cachedPrettyDayValue = '';
    let dayKeystrokes = 0;
    dayElement.addEventListener('focus', () => {
      cachedPrettyDayValue = '';
      dayKeystrokes = 0;
    });
    dayElement.addEventListener('keydown', event => {
      switch(event.code) {
        case 'Digit1':
        case 'Digit2':
        case 'Digit3':
        case 'Digit4':
        case 'Digit5':
        case 'Digit6':
        case 'Digit7':
        case 'Digit8':
        case 'Digit9':
        case 'Digit0': {
          event.preventDefault();
          dayKeystrokes++;
          const inputValue = Number(event.key);
          if (!cachedPrettyDayValue) {
            cachedPrettyDayValue = String(inputValue).padStart(2, "0");
            dayElement.textContent = cachedPrettyDayValue;
          } else {
            const next = Number(cachedPrettyDayValue + inputValue);
            // use greaterThan instead of equals to support keyboard digit input a la 33 which then gets transformed to 31
            // a month can maximally have a date of 31
            if (next > 31) {
              cachedPrettyDayValue = '31';
              dayElement.textContent = cachedPrettyDayValue;
            } else {
              cachedPrettyDayValue = String(next).padStart(2, '0');
              dayElement.textContent = cachedPrettyDayValue;
            }
          }
          if (dayKeystrokes === 2) {
            monthElement.focus();
          }
          this.updateNativeSearchInput();
          this.renderDatepicker();
          break;
        }
        case 'ArrowRight': {
          event.preventDefault();
          monthElement.focus();
          break;
        }
        case 'ArrowUp': {
          event.preventDefault();
          cachedPrettyDayValue = '';
          if (Number.isNaN(Number(dayElement.textContent))) {
            const todayDate = getDate(new Date());
            dayElement.textContent = String(todayDate).padStart(2, '0');
          } else {
            let next = Number(dayElement.textContent) + 1;
            if (next === 32) {
              next = 1;
            }
            dayElement.textContent = String(next).padStart(2, "0");
          }
          this.updateNativeSearchInput();
          this.renderDatepicker();
          break;
        }
        case 'ArrowDown': {
          event.preventDefault();
          cachedPrettyDayValue = '';
          if (Number.isNaN(Number(dayElement.textContent))) {
            const todayDate = getDate(new Date());
            dayElement.textContent = String(todayDate).padStart(2, '0');
          } else {
            let next = Number(dayElement.textContent) - 1;
            if (next === 0) {
              next = 31;
            }
            dayElement.textContent = String(next).padStart(2, "0");
          }
          this.updateNativeSearchInput();
          this.renderDatepicker();
          break;
        }
      }
    });

    let cachedMonthValue = '';
    let monthKeystrokes = 0;
    monthElement.addEventListener('focus', () => {
      cachedMonthValue = '';
      monthKeystrokes = 0;
    });
    monthElement.addEventListener('keydown', event => {
      switch(event.code) {
        case 'Digit1':
        case 'Digit2':
        case 'Digit3':
        case 'Digit4':
        case 'Digit5':
        case 'Digit6':
        case 'Digit7':
        case 'Digit8':
        case 'Digit9':
        case 'Digit0': {
          event.preventDefault();
          monthKeystrokes++;
          const inputValue = Number(event.key);
          if (!cachedMonthValue) {
            cachedMonthValue = String(inputValue).padStart(2, "0");
            monthElement.textContent = cachedMonthValue;
          } else {
            const next = Number(cachedMonthValue + inputValue);
            // use greaterThan instead of equals to support keyboard digit input a la 22 which then gets transformed to 12
            // the month cannot be greater than 12 ;-)
            if (next > 12) {
              cachedMonthValue = '12';
              monthElement.textContent = cachedMonthValue;
            } else {
              cachedMonthValue = String(next).padStart(2, '0');
              monthElement.textContent = cachedMonthValue;
            }
          }
          if (monthKeystrokes === 2) {
            yearElement.focus();
          }
          this.updateNativeSearchInput();
          this.renderDatepicker();
          break;
        }
        case 'ArrowLeft': {
          event.preventDefault();
          dayElement.focus();
          break;
        }
        case 'ArrowRight': {
          event.preventDefault();
          yearElement.focus();
          break;
        }
        case 'ArrowUp': {
          event.preventDefault();
          if (Number.isNaN(Number(monthElement.textContent))) {
            const todayMonth = getMonth(new Date());
            monthElement.textContent = String(todayMonth + 1).padStart(2, '0');
          } else {
            const nextPretty = Number(monthElement.textContent) + 1;
            if (nextPretty === 13) {
              monthElement.textContent = '01';
            } else {
              monthElement.textContent = String(nextPretty).padStart(2, "0");
            }
          }
          this.updateNativeSearchInput();
          this.renderDatepicker();
          break;
        }
        case 'ArrowDown': {
          event.preventDefault();
          if (Number.isNaN(Number(monthElement.textContent))) {
            const todayMonth = getMonth(new Date());
            monthElement.textContent = String(todayMonth + 1).padStart(2, '0');
          } else {
            const nextPretty = Number(monthElement.textContent) - 1;
            if (nextPretty === -1) {
              monthElement.textContent = '12';
            } else {
              monthElement.textContent = String(nextPretty).padStart(2, "0");
            }
          }
          this.updateNativeSearchInput();
          this.renderDatepicker();
          break;
        }
      }
    });

    let cachedYearValue = '';
    yearElement.addEventListener('focus', () => {
      cachedYearValue = '';
    });
    yearElement.addEventListener('keydown', event => {
      switch(event.code) {
        case 'Digit1':
        case 'Digit2':
        case 'Digit3':
        case 'Digit4':
        case 'Digit5':
        case 'Digit6':
        case 'Digit7':
        case 'Digit8':
        case 'Digit9':
        case 'Digit0': {
          event.preventDefault();
          cachedYearValue += Number(event.key);
          yearElement.textContent = cachedYearValue.padStart(4, "0");
          this.updateNativeSearchInput();
          this.renderDatepicker();
          break;
        }
        case 'ArrowLeft': {
          event.preventDefault();
          monthElement.focus();
          break;
        }
        case 'ArrowUp': {
          event.preventDefault();
          if (Number.isNaN(Number(yearElement.textContent))) {
            cachedYearValue = String((new Date()).getFullYear());
          } else {
            cachedYearValue = String(Number(yearElement.textContent) + 1);
          }
          yearElement.textContent = cachedYearValue.padStart(2, "0");
          this.updateNativeSearchInput();
          this.renderDatepicker();
          break;
        }
        case 'ArrowDown': {
          event.preventDefault();
          if (Number.isNaN(Number(yearElement.textContent))) {
            cachedYearValue = String((new Date()).getFullYear());
          } else {
            cachedYearValue = String(Number(yearElement.textContent) - 1);
          }
          yearElement.textContent = cachedYearValue.padStart(2, "0");
          this.updateNativeSearchInput();
          this.renderDatepicker();
          break;
        }
      }
    });

    // render input element as focused one when day, month or year is focused
    placeholder.addEventListener('focusin', () => {
      datepickerInputElement.classList.add('focused');
    });
    // withdraw focus style of input element when day, month or year is blurred
    placeholder.addEventListener('focusout', () => {
      datepickerInputElement.classList.remove('focused');
    });

    this.foo = element('div');
    this.foo.attachShadow({ mode: 'open' })
      .append(style, datepickerInputElement, placeholder);

    if (this.hasChildNodes()) {
      this.replaceChild(datepickerFacadeElement, this.firstElementChild);
      this.append(this.foo);
    } else {
      this.append(datepickerFacadeElement, this.foo);
    }
  }

  /**
   * returns the timestamp used to display the month and the year
   */
  getTimestampForCalendar () {
    const yearTextContent = this.yearPlaceholderElement.textContent;
    const monthTextContent = this.monthPlaceholderElement.textContent;
    const prettyYear = yearTextContent === 'yyyy' ? getYear(new Date()) : Number(yearTextContent);
    const prettyMonth = monthTextContent === 'mm' ? getMonth(new Date()) + 1 : Number(monthTextContent);
    return getTime(new Date(prettyYear, prettyMonth - 1, 1));
  };

  /**
   * timestamp used to display the selected date
   */
  getSelectedTimestampForCalendar () {
    const prettyYear = Number(this.yearPlaceholderElement.textContent);
    const prettyMonth = Number(this.monthPlaceholderElement.textContent);
    const prettyDay = Number(this.dayPlaceholderElement.textContent);

    if ([prettyYear, prettyMonth, prettyDay].some(Number.isNaN)) {
      return '';
    }

    const date = new Date(prettyYear, prettyMonth - 1, prettyDay);
    if (getDate(date) === prettyDay) {
      return getTime(date);
    }

    return '';
  };

  connectedCallback() {
    const showDatepicker = this.renderDatepicker.bind(this);
    const maybeHideDatepicker = event => {
      if (event.target !== this && !this.contains(event.target)) {
        this.hideDatepicker();
      }
    };

    this.dayPlaceholderElement.addEventListener('focus', showDatepicker);
    this.monthPlaceholderElement.addEventListener('focus', showDatepicker);
    this.yearPlaceholderElement.addEventListener('focus', showDatepicker);

    this.datepickerInputElement.addEventListener('click', () => {
      if ([this.dayPlaceholderElement, this.monthPlaceholderElement, this.yearPlaceholderElement].every(element => element !== this.foo.shadowRoot.activeElement)) {
        // stack #focus, otherwise the invocation has no impact
        // (maybe because we're in the middle of an event (☞ﾟヮﾟ)☞)
        setTimeout(() => {
          this.dayPlaceholderElement.focus();
        })
      }
    });

    this.datepickerInputElement.addEventListener('search', event => {
      if (!event.target.value) {
        this.dayPlaceholderElement.textContent = 'dd';
        this.monthPlaceholderElement.textContent = 'mm';
        this.yearPlaceholderElement.textContent = 'yyyy';
        this.renderDatepicker();
      }
    });

    // hide opened datepicker when use navigates with keyboard tab out of datepicker scope
    document.addEventListener('focusin', maybeHideDatepicker);
    // hide opened datepicker when user clicks somewhere outside the datepicker scope
    document.addEventListener('click', maybeHideDatepicker);

    const handleDatepickerClick = () => {
      this.datepickerInputElement.classList.add('focused')
    };

    this.datepickerPlaceholder.addEventListener('click', handleDatepickerClick);

    this.unsubscribe = () => {
      this.datepickerInputElement.removeEventListener('focus', showDatepicker);
      document.removeEventListener('focusin', maybeHideDatepicker);
      document.removeEventListener('click', maybeHideDatepicker);
      this.datepickerPlaceholder.removeEventListener('click', handleDatepickerClick);
    };
  }

  disconnectedCallback() {
    this.unsubscribe();
  }

  set value(timestamp) {
    // used as return value of the component
    this.datepickerFacadeElement.value = format(new Date(Number(timestamp)), 'YYYY-MM-DD');
    // used to display text to the user
    const date = new Date(Number(timestamp));
    this.dayPlaceholderElement.textContent = format(date, 'DD');
    this.monthPlaceholderElement.textContent = format(date, 'MM');
    this.yearPlaceholderElement.textContent = format(date, 'YYYY');
    this.datepickerInputElement.value = format(date, 'DD.MM.YYYY');
  }

  get value() {
    return this.datepickerFacadeElement.value;
  }

  updateNativeSearchInput() {
    this.datepickerInputElement.value = this.getSelectedTimestampForCalendar();
  }

  renderDatepicker() {
    if (this.datepickerElement) {
      const timestamp = this.getTimestampForCalendar();
      const selectedTimestamp = this.getSelectedTimestampForCalendar();

      this.datepickerElement.querySelector('#month').textContent = format(new Date(timestamp), 'MMMM YYYY');
      this.monthElement.setAttribute('timestamp', timestamp);
      this.monthElement.setAttribute('selected', selectedTimestamp);

      // given we don't have a selected timestamp
      // still set the input value to 'foo' to keep the (x) clear icon on the native input[search] field
      this.datepickerFacadeElement.value = selectedTimestamp ? selectedTimestamp : 'invalid';
    }
    else {
      const datepickerElement = this.datepickerElement = element('div');
      datepickerElement.classList.add('shadow', 'p-2', 'absolute', 'z-10');
      datepickerElement.style.width = '300px';
      datepickerElement.style.background = '#f9f9f9';

      const timestamp = this.getTimestampForCalendar();
      const selectedTimestamp = this.getSelectedTimestampForCalendar();

      datepickerElement.innerHTML = `
        <div class="flex flex-row mb-4" style="background-color: #f9f9f9">
          <button id="prev" tabindex="-1">Previous</button>
          <span id="month" class="flex-1 text-center font-semibold">${format(new Date(timestamp), 'MMMM YYYY')}</span>
          <button id="next" tabindex="-1">Next</button>
        </div>
        <uv-datepicker-month timestamp="${timestamp}" selected="${selectedTimestamp}"></uv-datepicker-month>
      `;

      datepickerElement.addEventListener('click', event => {
        if (event.target.id === 'prev') {
          const previousMonth = subMonths(new Date(Number(this.monthElement.getAttribute('timestamp'))), 1);
          this.monthTitleElement.textContent = format(previousMonth, 'MMMM YYYY');
          this.monthElement.setAttribute('timestamp', String(getTime(previousMonth)));
        } else if (event.target.id === 'next') {
          const nextMonth = addMonths(new Date(Number(this.monthElement.getAttribute('timestamp'))), 1);
          this.monthTitleElement.textContent = format(nextMonth, 'MMMM YYYY');
          this.monthElement.setAttribute('timestamp', String(getTime(nextMonth)));
        }
      });

      this.foo.shadowRoot.append(datepickerElement);
      this.monthTitleElement = datepickerElement.querySelector('#month');
      this.monthElement = datepickerElement.querySelector('uv-datepicker-month');

      this.monthElement.addEventListener('change', event => {
        this.setAttribute('data-timestamp', event.target.value);
        this.value = event.target.value;
        this.hideDatepicker();
      });
    }
  }

  hideDatepicker() {
    if (this.foo.shadowRoot.contains(this.datepickerElement)) {
      this.foo.shadowRoot.removeChild(this.datepickerElement);
    }
    delete this.datepickerElement;
  }
}

class DatepickerMonth extends HTMLElement {
  static get observedAttributes() { return ['timestamp', 'selected']; };

  constructor() {
    super();

    const style = element('style');
    style.innerHTML = `
      .w-full {
        width: 100%;
      }
      .text-center {
        text-align: center;
      }
      table {
        border-collapse: collapse;
      }
      .day {
        --background-color-left: #E5E5E5;
        --background-color-right: #E5E5E5;
        --font-color: #6F6F6F;
        padding: 0.75rem 0.125rem;
        background: linear-gradient(90deg, var(--background-color-left) 50%, var(--background-color-right) 50%);
        cursor: default;
        color: var(--font-color);
        cursor: pointer;
      }
      /* 'different-month' must be the first special day, so other special days are overriding this */
      .different-month {
        --background-color-left: #EFEFEF;
        --background-color-right: #EFEFEF;
        --font-color: #AFAFAF;
        opacity: .8;
      }
      .today,
      .day:hover:not([data-selectable=false]) {
        --background-color-left: #CFCFCF;
        --background-color-right: #CFCFCF;
      }
      .weekend {
        --background-color-left: #B5D0DF;
        --background-color-right: #B5D0DF;
        --font-color: #FFFFFF;
      }
      .selected {
        --background-color-left: purple;
        --background-color-right: purple;
        --font-color: #FFFFFF;
      }
    `;

    const thead = element('thead');
    const tbody = element('tbody');
    const table = element('table.w-full.text-center', [thead, tbody]);
    const wrapper = element('div', [table]);

    this.attachShadow({mode: 'open'})
      .append(style, wrapper);

    this.thead = thead;
    this.tbody = tbody;
    this.table = table;
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (name === 'timestamp' && oldValue !== newValue) {
      const selectedTimestamp = this.getAttribute('selected');
      const selectedDate = selectedTimestamp ? new Date(Number(selectedTimestamp)) : null;
      this.render({ date: new Date(Number(newValue)), selected: selectedDate  });
    }
    if (name === 'selected' && oldValue !== newValue) {
      const selectedDate = newValue ? new Date(Number(newValue)) : null;
      this.render({ date: new Date(Number(this.getAttribute('timestamp'))), selected: selectedDate });
    }
  }

  connectedCallback() {
    const handleTableClick = event => {
      if (event.target.tagName === 'TD' && event.target.hasAttribute('data-timestamp')) {
        this.selectedTimestamp = Number(event.target.dataset.timestamp);
        this.dispatchEvent(new CustomEvent('change'));
      }
    };

    this.table.addEventListener('click', handleTableClick);

    this.unsubscribe = () => {
      this.table.removeEventListener('click', handleTableClick);
    };
  }

  disconnectedCallback() {
    this.unsubscribe();
  }

  get value() {
    return this.selectedTimestamp;
  }

  renderDay({ currentMonth, date, selected }) {
    const today = isToday(date);
    const classNames = [
      'day',
      today && 'today',
      currentMonth !== getMonth(date) && 'different-month',
      isWeekend(date) && 'weekend',
      isSameDay(date, selected) && 'selected'
    ].filter(Boolean).join(' ');

    return `<td class="${classNames}" data-timestamp="${format(date, 'x')}"">${format(date, 'DD')}</td>`;
  }

  render({ date, selected }) {
    const currentMonth = getMonth(date);
    const firstOfMonth = startOfMonth(date);
    const firstOfWeek = startOfWeek(firstOfMonth);

    this.thead.innerHTML = `
      <tr>
        ${times(7).map(daysToAdd => `<th>${format(addDays(firstOfWeek, daysToAdd), 'dd')}</th>`).join('')}
      </tr>
    `;

    this.tbody.innerHTML = `
      <tr>
        ${times(7).map(daysToAdd => this.renderDay({ currentMonth, selected, date: addDays(firstOfWeek, daysToAdd) })).join('')}
      </tr>
      <tr>
        ${times(7).map(daysToAdd => this.renderDay({ currentMonth, selected, date: addWeeks(addDays(firstOfWeek, daysToAdd), 1) })).join('')}
      </tr>
      <tr>
        ${times(7).map(daysToAdd => this.renderDay({ currentMonth, selected, date: addWeeks(addDays(firstOfWeek, daysToAdd), 2) })).join('')}
      </tr>
      <tr>
        ${times(7).map(daysToAdd => this.renderDay({ currentMonth, selected, date: addWeeks(addDays(firstOfWeek, daysToAdd), 3) })).join('')}
      </tr>
      <tr>
        ${times(7).map(daysToAdd => this.renderDay({ currentMonth, selected, date: addWeeks(addDays(firstOfWeek, daysToAdd), 4) })).join('')}
      </tr>
      <tr>
        ${times(7).map(daysToAdd => this.renderDay({ currentMonth, selected, date: addWeeks(addDays(firstOfWeek, daysToAdd), 5) })).join('')}
      </tr>
    `;
  }
}

const times = (n) => ({
  map: callback => {
    let result = [];
    for (let i = 0; i < n; i++) {
      result.push(callback(i));
    }
    return result;
  }
});

customElements.define('uv-datepicker', Datepicker);
customElements.define('uv-datepicker-month', DatepickerMonth);
