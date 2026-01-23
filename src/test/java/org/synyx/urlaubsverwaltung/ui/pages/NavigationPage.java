package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;

public class NavigationPage {

    private static final String SICK_NOTES_SELECTOR = "[data-test-id=navigation-sick-notes-link]";
    private static final String SETTINGS_SELECTOR = "[data-test-id=navigation-settings-link]";
    private static final String PERSONS_SELECTOR = "[data-test-id=navigation-persons-link]";

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
     * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
     */
    public void clickSickNotes() {
        page.locator(SICK_NOTES_SELECTOR).click();
    }

    /**
     * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
     */
    public void clickSettings() {
        page.locator(SETTINGS_SELECTOR).click();
    }

    /**
     * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
     */
    public void clickPersons() {
        page.locator(PERSONS_SELECTOR).click();
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
         * If the user is only allowed to create new applications, then there is no popover button, but only this link.
         *
         * <p>
         * see {@link QuickAdd#togglePopover()} to open the popover element and click a navigation link afterward.
         */
        public void clickCreateNewApplication() {
            // UV can be configured, so that a user can only create new applications.
            // in this case there is no "quick-add" menu, but a simple link to create the new
            page.locator(PLAIN_APPLICATION_SELECTOR).click();
        }

        /**
         * The user can open a popover menu when there are multiple options
         * (e.g. create new application or a new sick-note).
         *
         * <p>
         * This element is NOT available when sick-note feature is disabled, for instance.
         */
        public void togglePopover() {
            // JavaScript is required currently to open the popover -> wait for fetched assets
            page.waitForLoadState(DOMCONTENTLOADED);
            page.locator(BUTTON_SELECTOR).click();
        }

        /**
         * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
         */
        public void clickPopoverNewApplication() {
            page.locator(APPLICATION_SELECTOR).click();
        }

        /**
         * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
         */
        public void clickPopoverNewOvertime() {
            page.locator(OVERTIME_SELECTOR).click();
        }

        /**
         * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
         */
        public void clickPopoverNewSickNote() {
            page.locator(SICKNOTE_SELECTOR).click();
        }
    }

    private record AvatarMenu(Page page) {

        private static final String AVATAR_SELECTOR = "[data-test-id=avatar]";
        private static final String LOGOUT_SELECTOR = "[data-test-id=logout]";

        void logout() {

            page.waitForLoadState(DOMCONTENTLOADED);
            page.locator(AVATAR_SELECTOR).click();

            // do not wait for page refresh, this doesn't work on CI with webkit (timeout...)
            // however, this should not be an issue since a logout requires a new login to interact with UV again,
            // which explicitly navigates to the login page.
            page.locator(LOGOUT_SELECTOR).click();

            page.context().clearCookies();
            page.context().clearPermissions();
        }
    }
}
