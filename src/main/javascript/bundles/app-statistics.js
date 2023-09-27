import "../js/common";
import * as Turbo from "@hotwired/turbo";
import "../components/details-dropdown";
import { initAutosubmit } from "../components/form";
import "../js/application/app-statistics";
import "../js/hotwire-turbo-progressbar";

initAutosubmit();

// opt-in to turbo with `data-turbo="true"`
Turbo.session.drive = false;
