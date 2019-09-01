<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<style>
    .flex {
        display: flex;
    }
    .flex-row {
        flex-direction: row;
    }
    .flex-column {
        flex-direction: column;
    }
    .items-center {
        align-items: center;
    }
    .items-stretch {
        align-items: stretch;
    }
    .justify-center {
        justify-content: center;
    }
    .visually-hidden {
        position: absolute !important;
        height: 1px;
        width: 1px;
        overflow: hidden;
        clip: rect(1px 1px 1px 1px); /* IE6, IE7 */
        clip: rect(1px, 1px, 1px, 1px);
        white-space: nowrap; /* added line */
    }

    .p-1 {
        padding: .25em;
    }
    .p-2 {
        padding: .5em;
    }
    .py-2 {
        padding-top: .5em;
        padding-bottom: .5em;
    }
    .p-4 {
        padding: 1em;
    }
    .px-4 {
        padding-left: 1em;
        padding-right: 1em;
    }
    .mb-4 {
        margin-bottom: 1em;
    }
    .mb-8 {
        margin-bottom: 2em;
    }
    .mx-2 {
        margin-left: .5em;
        margin-right: .5em;
    }
    .cursor-pointer {
        cursor: pointer;
    }

    .shadow {
        box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
    }
    .shadow-md {
        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
    }

    .bg-orange-200 {
        background-color: #feebc8;
    }
    .bg-orange-300 {
        background-color: #fbd38d;
    }

    .text-xl {
        font-size: 1.25rem;
    }
    .text-2xl {
        font-size: 1.5rem;
    }

    .text-center {
        text-align: center;
    }

    input[name="type"] + label {
        border-radius: 100%;
        transition: background-color ease-in-out 200ms;
    }
    input[name="type"]:focus + label,
    input[name="type"]:hover + label,
    input[name="type"]:checked + label {
        background-color: white;
    }

    textarea[name="text"] {
        resize: none;
        overflow: auto; /* remove default scrollbars */
        width: 100%;
        max-width: 800px;
        height: 60px;
    }
</style>

<form:form id="feedback-form" action="/web/feedback" method="post" class="mb-8 shadow bg-orange-200">
    <c:if test="${empty showFeedbackThankYou}">
    <div id="feedback-form-inputs" class="flex flex-column p-4">
        <div class="flex flex-row justify-center mb-4">
            <input type="radio" name="type" id="feedback-type-UGLY" value="UGLY" class="visually-hidden" required />
            <label for="feedback-type-UGLY" class="cursor-pointer px-4 py-2 mx-2 text-2xl">
                :-(
            </label>
            <input type="radio" name="type" id="feedback-type-BAD" value="BAD" class="visually-hidden" required />
            <label for="feedback-type-BAD" class="cursor-pointer px-4 py-2 mx-2 text-2xl">
                :-\
            </label>
            <input type="radio" name="type" id="feedback-type-GOOD" value="GOOD" class="visually-hidden" required />
            <label for="feedback-type-GOOD" class="cursor-pointer px-4 py-2 mx-2 text-2xl">
                :-)
            </label>
            <input type="radio" name="type" id="feedback-type-AWESOME" value="AWESOME" class="visually-hidden" required />
            <label for="feedback-type-AWESOME" class="cursor-pointer px-4 py-2 mx-2 text-2xl">
                :-D
            </label>
        </div>
        <div class="flex mb-4 justify-center">
            <textarea name="text" placeholder="Du m&ouml;chtest uns noch etwas mitteilen?" cols="80" wrap="off" class="p-2 text-2xl"></textarea>
        </div>
        <div class="flex justify-center">
            <button type="submit" class="btn btn-default">Feedback geben</button>
        </div>
    </div>
    </c:if>
    <c:set var="thankYouStyle" value="${showFeedbackThankYou ? 'block' : 'none'}" />
    <div id="feedback-form-thankyou" style="display: <c:out value="${thankYouStyle}"/>" class="p-8 text-2xl text-center">
        Wir danken dir f&uuml;r dein Feedback. Du bist gro&szlig;artig.
    </div>
</form:form>
