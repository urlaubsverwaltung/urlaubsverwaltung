import "../js/common";
import * as Turbo from "@hotwired/turbo";
import "../components/list/list-selectable";
import "../components/checkbox-card";
import "../components/form/checkbox-all";
import { initAutosubmit } from "../components/form";

initAutosubmit();

// opt-in to turbo with `data-turbo="true"`
Turbo.session.drive = false;
