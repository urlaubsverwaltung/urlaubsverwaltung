import "@ungap/custom-elements";
import * as Turbo from "@hotwired/turbo";
import "../components/feedback-box";
import { initAutosubmit } from "../components/form";
import "../components/info-banner";
import "../components/navigation";
import "../components/person-search";
import "../components/sticky";
import "../components/tablist";
import "../components/textarea";
import "../components/tooltip";
import "./date-fns-localized";
import { updateHtmlElementAttributes } from "./html-element";

// opt-in to turbo with `data-turbo="true"`
Turbo.session.drive = false;

initAutosubmit();
updateHtmlElementAttributes(document);
