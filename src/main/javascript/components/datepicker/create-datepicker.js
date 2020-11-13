import createDatepickerInstances from "./create-datepicker-instances";

export async function createDatepicker(selector, { urlPrefix, getPersonId, onSelect }) {
  const [datepicker] = await createDatepickerInstances([selector], urlPrefix, getPersonId, onSelect);
  return datepicker.value;
}
