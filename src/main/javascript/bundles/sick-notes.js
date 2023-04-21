import * as Turbo from "@hotwired/turbo";
import "../components/details-dropdown";
import { initAutosubmit } from "../components/form";
import "../js/sick-notes/sick-days";
import "../js/hotwire-turbo-progressbar";
import "../js/navigate";

initAutosubmit();

// opt-in to turbo with `data-turbo="true"`
Turbo.session.drive = false;
