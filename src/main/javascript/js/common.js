import $ from "jquery";
import "../lib/bootstrap";
import "../components/avatar";
import "../components/navigation";
import "../components/table-selectable";
import "../components/textarea";
import "../components/feedback";
import tooltip from "../components/tooltip";
import "@ungap/custom-elements";
import "datalist-polyfill";
import "./date-fns-localized";

window.$ = $;
window.jQuery = $;

tooltip();
