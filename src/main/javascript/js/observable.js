/**
 * @typedef {{ value: *, subscribe: (callback) => () => void }} observable
 */

/**
 * Creates an observable container object.
 *
 * @param {*} initial
 * @return {observable}
 */
export function observable(initial) {
  let value = initial;
  let subscriptions = [];
  return Object.defineProperties(
    {},
    {
      value: {
        get() {
          return value;
        },
        set(_value) {
          const previous = value;
          value = _value;
          for (let f of subscriptions) {
            try {
              f(value, previous);
            } catch (error) {
              // eslint-disable-next-line no-undef
              if (process.env.NODE_ENV !== "test") {
                console.error("catched error of observable subscribtion", error);
              }
            }
          }
        },
      },
      subscribe: {
        value: function (callback) {
          subscriptions.push(callback);
          return function () {
            subscriptions = subscriptions.filter((s) => s !== callback);
          };
        },
      },
    },
  );
}
