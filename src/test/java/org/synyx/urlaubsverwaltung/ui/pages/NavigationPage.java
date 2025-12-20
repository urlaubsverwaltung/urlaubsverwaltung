package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.options.WaitForSelectorState.ATTACHED;
import static com.microsoft.playwright.options.WaitForSelectorState.DETACHED;

public class NavigationPage {

    private static final String NAVIGATION_SELECTOR = "div.navigation";
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

    public void isVisible() {
        page.waitForSelector(NAVIGATION_SELECTOR);
    }

    public void logout() {
        avatarMenu.logout();
    }

    public void clickSickNotes() {
        page.locator(SICK_NOTES_SELECTOR).click();
    }

    public void clickSettings() {
        page.locator(SETTINGS_SELECTOR).click();
    }

    public static class QuickAdd {
        private static final String PLAIN_APPLICATION_SELECTOR = "[data-test-id=new-application]";
        private static final String BUTTON_SELECTOR = "[data-test-id=add-something-new]";
        private static final String POPUPMENU_SELECTOR = "[data-test-id=add-something-new-popupmenu]";
        private static final String APPLICATION_SELECTOR = "[data-test-id=quick-add-new-application]";
        private static final String SICKNOTE_SELECTOR = "[data-test-id=quick-add-new-sicknote]";
        private static final String OVERTIME_SELECTOR = "[data-test-id=quick-add-new-overtime]";

        private final Page page;

        private QuickAdd(Page page) {
            this.page = page;
        }

        public boolean hasPopup() {
            page.locator(POPUPMENU_SELECTOR).waitFor(new Locator.WaitForOptions().setState(ATTACHED));
            page.locator(PLAIN_APPLICATION_SELECTOR).waitFor(new Locator.WaitForOptions().setState(DETACHED));
            return true;
        }

        public boolean hasNoPopup() {
            page.locator(POPUPMENU_SELECTOR).waitFor(new Locator.WaitForOptions().setState(DETACHED));
            page.locator(PLAIN_APPLICATION_SELECTOR).waitFor(new Locator.WaitForOptions().setState(ATTACHED));
            return true;
        }

        public void click() {
            if (page.locator(BUTTON_SELECTOR).isVisible()) {
                // opens the menu that contains links to new pages
                page.locator(BUTTON_SELECTOR).click();
            } else if (page.locator(PLAIN_APPLICATION_SELECTOR).isVisible()) {
                page.locator(PLAIN_APPLICATION_SELECTOR).click();
            }
        }

        public void newApplication() {
            page.waitForSelector(APPLICATION_SELECTOR).click();
        }

        public void newOvertime() {
            page.waitForSelector(OVERTIME_SELECTOR).click();
        }

        public void newSickNote() {
            page.waitForSelector(SICKNOTE_SELECTOR).click();
        }
    }

    private record AvatarMenu(Page page) {

        private static final String AVATAR_SELECTOR = "[data-test-id=avatar]";
        private static final String LOGOUT_SELECTOR = "[data-test-id=logout]";

        void logout() {
            page.locator(AVATAR_SELECTOR).click();
            page.waitForSelector(LOGOUT_SELECTOR).click();

            page.context().clearCookies();
            page.context().clearPermissions();
            page.evaluate("window.localStorage.clear();");
            page.evaluate("window.sessionStorage.clear();");
        }
    }
}
