import * as Turbo from "@hotwired/turbo";
import count from "../js/count";
import maxChars from "../js/max-chars";
import { initAutosubmit } from "../components/form";

initAutosubmit();

// opt-in to turbo with `data-turbo="true"`
Turbo.session.drive = false;

window.count = count;
window.maxChars = maxChars;
