import $ from 'jquery'
import 'bootstrap/js/tab'

(function() {

  /**
   * when a anchor is defined in the url (#)
   * then it will be opened.
   */
  function activateTabFromAnchorLink() {
    const url = window.location.href;
    const tabName = url.split('#')[1];
    if (tabName) {
      activeTab(tabName);
    }
  }

  function activeTab(tab) {
    $('.nav-tabs a[href="#' + tab + '"]').tab('show');
  }

  $(document).ready(function () {
    activateTabFromAnchorLink();
  });

})();
