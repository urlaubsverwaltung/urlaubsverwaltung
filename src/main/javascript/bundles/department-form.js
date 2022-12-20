import * as Turbo from "@hotwired/turbo";
import count from "../js/count";
import maxChars from "../js/max-chars";
import "../components/form/autosubmit";

// opt-in to turbo with `data-turbo="true"`
Turbo.session.drive = false;

window.count = count;
window.maxChars = maxChars;
