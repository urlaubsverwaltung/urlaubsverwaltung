import "../lib/bootstrap";
import "../components/navigation";
import "../components/textarea";
import "../components/feedback";
import tooltip from "../components/tooltip";
import "@ungap/custom-elements";
import "./date-fns-localized";
import { updateHtmlElementAttributes } from "./html-element";

tooltip();
updateHtmlElementAttributes(document);
