import { createDatepicker } from "../create-datepicker";
import createDatepickerInstancesMock from "../create-datepicker-instances";

jest.mock("../create-datepicker-instances", () => {
  return jest.fn();
});

describe("create-datepicker", () => {
  afterEach(async () => {
    // cleanup DOM
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
  });

  test('invokes "create-datepicker-instances" and returns the datepicker element', async () => {
    document.body.innerHTML = `
      <input id="just-a-date-picker" />
    `;

    const urlPrefix = "/prefix/";
    const getPersonId = () => "1337";
    const onSelect = () => {};

    createDatepickerInstancesMock.mockResolvedValue([
      {
        status: "fulfilled",
        value: "returned datepicker element",
      },
    ]);

    const result = await createDatepicker("#just-a-datepicker", { urlPrefix, getPersonId, onSelect });

    expect(result).toEqual("returned datepicker element");
    expect(createDatepickerInstancesMock).toHaveBeenCalledWith(
      ["#just-a-datepicker"],
      "/prefix/",
      expect.any(Function),
      expect.any(Function),
    );
    expect(createDatepickerInstancesMock.mock.calls[0][2]).toBe(getPersonId);
    expect(createDatepickerInstancesMock.mock.calls[0][3]).toBe(onSelect);
  });
});
