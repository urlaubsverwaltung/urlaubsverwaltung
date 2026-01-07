package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;

import static org.synyx.urlaubsverwaltung.ui.pages.UvPage.executeAndWaitForPageRefresh;

public class NavigationPage {

    private static final String SICK_NOTES_SELECTOR = "[data-test-id=navigation-sick-notes-link]";
    private static final String SETTINGS_SELECTOR = "[data-test-id=navigation-settings-link]";

    private final Page page;
    private final AvatarMenu avatarMenu;
    public final QuickAdd quickAdd;

    public NavigationPage(Page page) {
        this.avatarMenu = new AvatarMenu(page);
        this.quickAdd = new QuickAdd(page);
        this.page = page;
    }

    public void logout() {
        avatarMenu.logout();
    }

    /**
     * Clicks sick-notes link and waits for page refresh.
     */
    public void goToSickNotes() {
        executeAndWaitForPageRefresh(page, page ->
            page.locator(SICK_NOTES_SELECTOR).click());
    }

    /**
     * Clicks settings link and waits for page refresh.
     */
    public void goToSettings() {
        executeAndWaitForPageRefresh(page, page ->
            page.locator(SETTINGS_SELECTOR).click());
    }

    public static class QuickAdd {
        private static final String PLAIN_APPLICATION_SELECTOR = "[data-test-id=new-application]";
        private static final String BUTTON_SELECTOR = "[data-test-id=add-something-new]";
        private static final String APPLICATION_SELECTOR = "[data-test-id=quick-add-new-application]";
        private static final String SICKNOTE_SELECTOR = "[data-test-id=quick-add-new-sicknote]";
        private static final String OVERTIME_SELECTOR = "[data-test-id=quick-add-new-overtime]";

        private final Page page;

        private QuickAdd(Page page) {
            this.page = page;
        }

        /**
         * The user can open a popover menu when there are multiple options
         * (e.g. create new application or a new sick-note).
         *
         * <p>
         * This element is NOT available when sick-note feature is disabled, for instance.
         */
        public void togglePopover() {
            page.locator(BUTTON_SELECTOR).click();
        }

        /**
         * If the user is only allowed to create new applications, then there is no popover button, but only this link.
         */
        public void clickCreateNewApplication() {
            // UV can be configured, so that a user can only create new applications.
            // in this case there is no "quick-add" menu, but a simple link to create the new
            executeAndWaitForPageRefresh(page, page ->
                page.locator(PLAIN_APPLICATION_SELECTOR).click());
        }

        public void clickPopoverNewApplication() {
            executeAndWaitForPageRefresh(page, page ->
                page.waitForSelector(APPLICATION_SELECTOR).click());
        }

        public void clickPopoverNewOvertime() {
            executeAndWaitForPageRefresh(page, page ->
                page.waitForSelector(OVERTIME_SELECTOR).click());
        }

        public void clickPopoverNewSickNote() {
            executeAndWaitForPageRefresh(page, page ->
                page.waitForSelector(SICKNOTE_SELECTOR).click());
        }
    }

    private record AvatarMenu(Page page) {

        private static final String AVATAR_SELECTOR = "[data-test-id=avatar]";
        private static final String LOGOUT_SELECTOR = "[data-test-id=logout]";

        void logout() {

            page.locator(AVATAR_SELECTOR).click();

            // do not wait for page refresh, this doesn't work on CI with webkit (timeout...)
            // however, this should not be an issue since a logout requires a login to interact with UV,
            // which explicitly navigates to the login page.
            page.waitForSelector(LOGOUT_SELECTOR).click();

            page.context().clearCookies();
            page.context().clearPermissions();
            page.evaluate("window.localStorage.clear();");
            page.evaluate("window.sessionStorage.clear();");
        }
    }
}
