jest.setMock('../../js/fetch', {
  postJSON: jest.fn()
});

describe('user feedback form', () => {
  afterEach(() => {
    while (document.body.firstChild) {
      document.body.removeChild(document.body.firstChild);
    }
    jest.resetModules();
    jest.resetAllMocks();
  });

  it('prevents default behaviour on form submit', () => {
    const { form } = setup();

    // interaction elements
    form.type = { value: 'AWESOME' };
    form.text = { value: 'some text entered by the user' };

    const submitEvent = new Event('submit');
    jest.spyOn(submitEvent, 'preventDefault');

    form.dispatchEvent(submitEvent);

    expect(submitEvent.preventDefault).toHaveBeenCalled();
  });

  it('posts the data on form submit', () => {
    // we have to import "postJSON" here instead on top level since every test gets it's own fresh instances
    // import on top level is always the same reference for the lifetime of this spec module
    // but we're importing a new "user-feedback-form" module for every test which references a new "fetch" module
    const { postJSON } = require("../../js/fetch");
    const { form } = setup();

    // interaction elements
    form.type = { value: 'AWESOME' };
    form.text = { value: 'some text entered by the user' };

    expect(postJSON).not.toHaveBeenCalled();

    const submitEvent = new Event('submit');
    form.dispatchEvent(submitEvent);

    expect(postJSON).toHaveBeenCalledWith('/api/feedback', {
      type: 'AWESOME',
      text: 'some text entered by the user'
    });
  });

  it('toggles visible element to show "thank you"', () => {
    const { form, formInputs, formThankYou } = setup();

    // interaction elements
    form.type = { value: 'AWESOME' };
    form.text = { value: 'some text entered by the user' };

    expect(formInputs.style.display).not.toEqual('none');
    expect(formThankYou.style.display).not.toEqual('block');

    const submitEvent = new Event('submit');
    form.dispatchEvent(submitEvent);

    expect(formInputs.style.display).toEqual('none');
    expect(formThankYou.style.display).toEqual('block');
  });

  function setup() {
    // setup DOM elements
    const form = h('form', { id: 'feedback-form' });
    const formInputs = h('div', { id: 'feedback-form-inputs' });
    const formThankYou = h('div', { id: 'feedback-form-thankyou' });

    form.append(formInputs, formThankYou);
    document.body.append(form);

    // load script
    require('../user-feedback-form');

    return { form, formInputs, formThankYou };
  }

  function h(tagName, attributes) {
    const element = document.createElement(tagName);
    for (let [key, value] of Object.entries(attributes)) {
      element[key] = value;
    }
    return element;
  }
});
