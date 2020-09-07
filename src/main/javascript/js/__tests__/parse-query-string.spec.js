import parseQueryString from './../parse-query-string';

describe('parseQueryString', () => {
  it('returns object of window.location.search', () => {
    const parsed = parseQueryString("?one=eins&two=zwei");
    expect(parsed).toEqual({ one: 'eins', two: 'zwei' });
  });
});
