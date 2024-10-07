import "../js/common";
import * as Turbo from "@hotwired/turbo";
import "../components/details-dropdown";
import "../js/sick-notes/sick-days";
import "../js/hotwire-turbo-progressbar";
import { initAutosubmit } from "../components/form";
import { navigate } from "../js/navigate";

globalThis.navigate = navigate;

initAutosubmit();

// opt-in to turbo with `data-turbo="true"`
Turbo.session.drive = false;
