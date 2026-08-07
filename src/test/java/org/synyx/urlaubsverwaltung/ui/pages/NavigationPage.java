package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;

public class NavigationPage {

    private static final String SICK_NOTES_SELECTOR = "[data-test-id=navigation-sick-notes-link]";
    private static final String PERSONS_SELECTOR = "[data-test-id=navigation-persons-link]";
    private static final String MOBILE_MENU_TOGGLE_SELECTOR = "button[popovertarget=navigation]";
    private static final String MOBILE_MENU_SELECTOR = "#navigation";
    private static final String OVERVIEW_LINK_SELECTOR = "#basic-overview-link";

    private final Page page;
    private final AvatarMenu avatarMenu;
    public final QuickAdd quickAdd;
    public final SettingsMenu settingsMenu;

    public NavigationPage(Page page) {
        this.avatarMenu = new AvatarMenu(page);
        this.quickAdd = new QuickAdd(page);
        this.settingsMenu = new SettingsMenu(page);
        this.page = page;
    }

    public void logout() {
        avatarMenu.logout();
    }

    /**
     * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
     */
    public void clickSickNotes() {
        // this navigation item has sub items, therefore it is rendered as a `button[data-href]` and not as an `a[href]`.
        // such a button is navigated by the click handler of navigation.js, so clicking it before the javascript of the
        // page has been evaluated is silently swallowed and the browser stays on the current page. waiting for
        // DOMContentLoaded guarantees the deferred module scripts ran and the handler is registered.
        page.waitForLoadState(DOMCONTENTLOADED);
        page.locator(SICK_NOTES_SELECTOR).click();
    }

    /**
     * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
     */
    public void clickPersons() {
        page.locator(PERSONS_SELECTOR).click();
    }

    /**
     * Opens the mobile navigation drawer (the hamburger menu button, only visible below the desktop breakpoint).
     */
    public void openMobileMenu() {
        page.locator(MOBILE_MENU_TOGGLE_SELECTOR).click();
    }

    /**
     * The mobile navigation drawer element itself.
     */
    public Locator mobileMenu() {
        return page.locator(MOBILE_MENU_SELECTOR);
    }

    /**
     * A navigation link that is visible to every authenticated person, regardless of role. Useful to assert
     * that the navigation drawer actually renders its content and is not just an empty/collapsed box.
     */
    public Locator overviewLink() {
        return page.locator(OVERVIEW_LINK_SELECTOR);
    }

    public static final class QuickAdd {
        private static final String APPLICATION_SELECTOR = "[data-test-id=create-application-link]";
        private static final String SICKNOTE_SELECTOR = "[data-test-id=create-sicknote-link]";
        private static final String OVERTIME_SELECTOR = "[data-test-id=create-overtime-link]";

        private final Page page;

        private QuickAdd(Page page) {
            this.page = page;
        }

        /**
         * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
         */
        public void clickCreateNewApplication() {
            page.locator(APPLICATION_SELECTOR).click();
        }

        /**
         * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
         */
        public void clickCreateNewOvertime() {
            page.locator(OVERTIME_SELECTOR).click();
        }

        /**
         * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
         */
        public void clickCreateNewSickNote() {
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

    public record SettingsMenu(Page page) {

        private static final String SETTINGS_OVERTIME_SELECTOR = "[data-test-id=settings-overtime-link]";
        private static final String SETTINGS_ABSENCE_SELECTOR = "[data-test-id=settings-absence-link]";
        private static final String SETTINGS_ABSENCE_TYPES_SELECTOR = "[data-test-id=settings-absencetypes-link]";

        /**
         * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
         */
        public void clickOvertime() {
            page.locator(SETTINGS_OVERTIME_SELECTOR).click();
        }

        /**
         * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
         */
        public void clickAbsence() {
            page.locator(SETTINGS_ABSENCE_SELECTOR).click();
        }

        /**
         * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
         */
        public void clickAbsenceTypes() {
            page.locator(SETTINGS_ABSENCE_TYPES_SELECTOR).click();
        }
    }
}
