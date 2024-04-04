import * as Turbo from "@hotwired/turbo";
import "../js/common";
import "../components/color-picker";
import "../components/datepicker";
import "../components/tabs";
import "../js/settings/settings-form";
import "../js/settings/absence-types";
import "../js/settings/special-leave-table";

// opt-in to turbo with `data-turbo="true"`
Turbo.session.drive = false;
