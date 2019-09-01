import fetchMock from 'fetch-mock';
import {getJSON, postJSON} from '../fetch';

describe('fetch', () => {
  afterEach(function () {
      fetchMock.restore();
  });

  describe('getJSON', () => {
    it('fetches contentType=json with GET method', async () => {
      fetchMock.mock('https://awesome-stuff.com/api/give-me-json', {});

      await getJSON('https://awesome-stuff.com/api/give-me-json');

      expect(fetchMock.lastUrl()).toEqual('https://awesome-stuff.com/api/give-me-json');
      expect(fetchMock.lastOptions()).toEqual({
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        },
      });
    });

    it('returns json parsed response', async () => {
      fetchMock.mock('https://awesome-stuff.com/api/give-me-json', {
        data: {
          hero: 'batman'
        }
      });

      const json = await getJSON('https://awesome-stuff.com/api/give-me-json');
      expect(json.data).toEqual({ hero: 'batman' });
    });

    it('throws when server returns "not ok"', async () => {
      expect.hasAssertions();

      fetchMock.mock('https://awesome-stuff.com/api/give-me-json', 500);

      try {
        await getJSON('https://awesome-stuff.com/api/give-me-json');
      } catch(error) {
        expect(error).toEqual(expect.objectContaining({
          status: 500,
          statusText: 'Internal Server Error',
          url: 'https://awesome-stuff.com/api/give-me-json'
        }));
      }
    });
  });

  describe('postJSON', () => {
    it('posts given data as contentType=json', async () => {
      fetchMock.mock('https://awesome-stuff.com/api/post-something', {});

      await postJSON('https://awesome-stuff.com/api/post-something', {
        some: 'attributes'
      });

      expect(fetchMock.lastUrl()).toEqual('https://awesome-stuff.com/api/post-something');
      expect(fetchMock.lastOptions()).toEqual(expect.objectContaining({
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: '{"some":"attributes"}'
      }));
    });

    it('includes credentials', async () => {
      fetchMock.mock('https://awesome-stuff.com/api/post-something', {});

      await postJSON('https://awesome-stuff.com/api/post-something', {});

      expect(fetchMock.lastOptions()).toEqual(expect.objectContaining({
        credentials: 'same-origin'
      }));
    });

    it('includes csrf token', async () => {
      const header = document.createElement('meta');
      header.name = '_csrf_header';
      header.content = 'X-CSRF-TOKEN-FOO';
      document.head.append(header);

      const token = document.createElement('meta');
      token.name = '_csrf';
      token.content = '1234567890';
      document.head.append(token);

      fetchMock.mock('https://awesome-stuff.com/api/post-something', {});

      await postJSON('https://awesome-stuff.com/api/post-something', {});

      expect(fetchMock.lastOptions()).toEqual(expect.objectContaining({
        headers: {
          'Content-Type': 'application/json',
          'X-CSRF-TOKEN-FOO': '1234567890'
        }
      }));
    });

    it('returns nothing', async () => {
      fetchMock.mock('https://awesome-stuff.com/api/post-something', {
        data: {
          hero: 'batman'
        }
      });

      const json = await postJSON('https://awesome-stuff.com/api/post-something', {});
      expect(json).toBeUndefined();
    });

    it('throws when server returns "not ok"', async () => {
      expect.hasAssertions();

      fetchMock.mock('https://awesome-stuff.com/api/post-something', 500);

      try {
        await postJSON('https://awesome-stuff.com/api/post-something', {});
      } catch(error) {
        expect(error).toEqual(expect.objectContaining({
          status: 500,
          statusText: 'Internal Server Error',
          url: 'https://awesome-stuff.com/api/post-something'
        }));
      }
    });
  });
});
