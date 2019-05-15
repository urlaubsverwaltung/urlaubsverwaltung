import html from '../html-literal'

describe('html-literal', () => {
  it('returns processed string', () => {
    const title = "awesome stuff!";
    const actual = html`<h1>${title}</h1>`;
    expect(actual).toMatchSnapshot();
  });

  it('returns processed string with array substitutions', () => {
    const personList = [
      'Bruce',
      'Clark'
    ];
    const actual = html`<ul>${personList.map(person => `<li>${person}</li>`)}</ul>`;
    expect(actual).toMatchSnapshot();
  });
});
