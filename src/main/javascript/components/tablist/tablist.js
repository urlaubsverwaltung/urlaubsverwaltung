/*
 * <ul role="tablist">
 *   <li>
 *     <button role="tab" aria-controls="tab-content-1">Tab 1</button>
 *   </li>
 *   <li>
 *     <a role="tab" aria-controls="tab-content-2">Tab 2</button>
 *   </li>
 * </ul>
 * <div role="tabpanel" id="tab-content-1">Tab Content 1</div>
 * <div role="tabpanel" id="tab-content-2">Tab Content 2</div>
 */

document.addEventListener("click", function (event) {
  /** @type Element  */
  const target = event.target;
  const tabList = target.closest("[role=tablist]");
  if (tabList) {
    const clickedTab = target.closest("[role=tab]");
    if (clickedTab) {
      // toggle active tab
      for (let tab of getTabs(tabList)) {
        tab.classList.toggle("tab--active", tab === clickedTab);
      }

      // toggle active tab-panel
      const tabPanelId = clickedTab.getAttribute("aria-controls");
      const tabPanelToShow = document.querySelector("#" + tabPanelId);
      if (tabPanelToShow) {
        event.preventDefault();
        // only one tabPanel can be visible. set class on active panel, remove on other panels
        for (let tabPanel of getTabPanels(tabList)) {
          tabPanel.classList.toggle("tab-panel--active", tabPanel === tabPanelToShow);
        }
      } else {
        console.warn("Could not find tabPanel with id=%s", tabPanelId);
      }
    }
  }
});

/**
 * @param {HTMLElement} tabList
 * @return {HTMLElement[]}
 */
function getTabs(tabList) {
  return [...tabList.querySelectorAll("[role=tab]")];
}

/**
 * @param {HTMLElement} tabList
 * @return {HTMLElement[]}
 */
function getTabPanels(tabList) {
  return [...tabList.querySelectorAll("[role=tab]")]
    .map((tab) => tab.getAttribute("aria-controls"))
    .map((id) => document.querySelector("#" + id));
}
