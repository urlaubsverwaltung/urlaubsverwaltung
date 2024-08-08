import "../js/common";
import * as Turbo from "@hotwired/turbo";
import "../components/back-button";
import { initAutosubmit } from "../components/form";

initAutosubmit();

// opt-in to turbo with `data-turbo="true"`
Turbo.session.drive = false;
