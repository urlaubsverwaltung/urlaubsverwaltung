import { signalLocaleChange, subscribeToLocaleChanged } from './locale-resolver';

describe('date-fns locale-resolver', () => {
  afterEach(() => {
    window.__datefnsLocaleChangedListener__ = [];
  });

  it('signalLocaleChange invokes all registered listeners', () => {
    const listener1 = jest.fn();
    const listener2 = jest.fn();

    subscribeToLocaleChanged(listener1);
    subscribeToLocaleChanged(listener2);

    signalLocaleChange();

    expect(listener1).toHaveBeenCalledTimes(1);
    expect(listener2).toHaveBeenCalledTimes(1);
  });
});
