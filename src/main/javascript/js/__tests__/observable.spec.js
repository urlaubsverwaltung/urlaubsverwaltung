import { observable } from "../observable";

describe("observable", () => {
  test("creates an observable with an initial value", () => {
    const sut = observable("test");
    expect(sut.value).toBe("test");
  });

  test("subscribes to changes", () => {
    const sut = observable("initial");
    const callback = jest.fn();

    sut.subscribe(callback);
    sut.value = "updated";

    expect(callback).toHaveBeenCalledTimes(1);
    expect(callback).toHaveBeenCalledWith("updated", "initial");
  });

  test("notifies multiple subscribers", () => {
    const sut = observable(1);
    const callback1 = jest.fn();
    const callback2 = jest.fn();

    sut.subscribe(callback1);
    sut.subscribe(callback2);
    sut.value = 2;

    expect(callback1).toHaveBeenCalledWith(2, 1);
    expect(callback2).toHaveBeenCalledWith(2, 1);
  });

  test("unsubscribes correctly", () => {
    const sut = observable("test");
    const callback = jest.fn();

    const unsubscribe = sut.subscribe(callback);
    sut.value = "first update";

    expect(callback).toHaveBeenCalledTimes(1);

    unsubscribe();
    sut.value = "second update";

    expect(callback).toHaveBeenCalledTimes(1);
  });

  test("subscriber errors don't affect other subscribers", () => {
    const sut = observable("test");
    const callback1 = jest.fn().mockImplementation(() => {
      throw new Error("Subscriber error");
    });
    const callback2 = jest.fn();

    sut.subscribe(callback1);
    sut.subscribe(callback2);

    sut.value = "updated";

    expect(callback2).toHaveBeenCalledWith("updated", "test");
  });
});
