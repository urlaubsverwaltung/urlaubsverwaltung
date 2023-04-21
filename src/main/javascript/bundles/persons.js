import * as Turbo from "@hotwired/turbo";
import { initAutosubmit } from "../components/form";
import "../js/hotwire-turbo-progressbar";
import "../js/navigate";

initAutosubmit();

// opt-in to turbo with `data-turbo="true"`
Turbo.session.drive = false;
