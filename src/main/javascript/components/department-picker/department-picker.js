/*
 * Stages a department selection until it is confirmed. Rendered by `fragments/department-picker.html`:
 *
 * <uv-department-picker>
 *   <button type="button" popovertarget="x-popover">…</button>
 *   <div id="x-popover" popover>
 *     <input type="search" data-department-picker-search />            (optional)
 *     <input type="checkbox" name="allPersons" data-department-picker-all />
 *     <ul>
 *       <li><input type="checkbox" name="department" data-department-picker-department /></li>
 *       <li data-department-picker-no-hit hidden>…</li>
 *     </ul>
 *     <button type="submit" data-department-picker-confirm>…</button>
 *   </div>
 * </uv-department-picker>
 */
function preventSubmitOnEnter(event) {
  // enter in the search field would submit the staged selection of the host form
  if (event.key === "Enter") {
    event.preventDefault();
  }
}

export class DepartmentPicker extends HTMLElement {
  #cleanup = () => {};

  connectedCallback() {
    const popover = this.querySelector("[popover]");
    const all = this.querySelector("[data-department-picker-all]");
    const departments = [...this.querySelectorAll("[data-department-picker-department]")];
    const search = this.querySelector("[data-department-picker-search]");
    const noHit = this.querySelector("[data-department-picker-no-hit]");
    const confirm = this.querySelector("[data-department-picker-confirm]");
    const checkboxes = [all, ...departments];

    let snapshot = checkboxes.map((checkbox) => checkbox.checked);
    let confirmed = false;

    const filter = (query) => {
      const normalizedQuery = query.trim().toLocaleLowerCase();
      let hits = 0;
      for (const department of departments) {
        const hit = department.value.toLocaleLowerCase().includes(normalizedQuery);
        department.closest("li").hidden = !hit;
        hits += hit ? 1 : 0;
      }
      noHit.hidden = hits > 0;
    };

    const handleChange = (event) => {
      // the selection is staged until it is confirmed - a host form submitting on change must not see it
      event.stopPropagation();

      const { target } = event;
      const noDepartmentChecked = departments.every((department) => !department.checked);
      if (target === all) {
        if (all.checked) {
          for (const department of departments) {
            department.checked = false;
          }
        } else if (noDepartmentChecked) {
          // an empty selection falls back to the own departments on the server - keep "all persons" instead
          all.checked = true;
        }
      } else if (departments.includes(target)) {
        if (target.checked) {
          all.checked = false;
        } else if (noDepartmentChecked) {
          all.checked = true;
        }
      }
    };

    const handleToggle = (event) => {
      if (event.newState === "open") {
        snapshot = checkboxes.map((checkbox) => checkbox.checked);
        confirmed = false;
        if (search) {
          search.value = "";
          filter("");
        }
      } else if (!confirmed) {
        for (const [index, checkbox] of checkboxes.entries()) {
          checkbox.checked = snapshot[index];
        }
      }
    };

    const handleConfirm = () => {
      confirmed = true;
    };

    const handleSearchInput = () => filter(search.value);

    this.addEventListener("change", handleChange);
    popover.addEventListener("toggle", handleToggle);
    confirm.addEventListener("click", handleConfirm);
    search?.addEventListener("input", handleSearchInput);
    search?.addEventListener("keydown", preventSubmitOnEnter);

    this.#cleanup = () => {
      this.removeEventListener("change", handleChange);
      popover.removeEventListener("toggle", handleToggle);
      confirm.removeEventListener("click", handleConfirm);
      search?.removeEventListener("input", handleSearchInput);
      search?.removeEventListener("keydown", preventSubmitOnEnter);
    };
  }

  disconnectedCallback() {
    this.#cleanup();
  }
}

customElements.define("uv-department-picker", DepartmentPicker);
