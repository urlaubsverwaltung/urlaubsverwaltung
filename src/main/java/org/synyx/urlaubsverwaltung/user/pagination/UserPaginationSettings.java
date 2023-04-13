package org.synyx.urlaubsverwaltung.user.pagination;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableArgumentResolver;

/**
 * Fallback user aware values for {@linkplain Pageable}. Typically use by a {@linkplain  PageableArgumentResolver}.
 */
public final class UserPaginationSettings {

    private final int defaultPageSize;

    UserPaginationSettings(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public static Builder builder(UserPaginationSettings userPaginationSettings) {
        return new Builder()
            .defaultPageSize(userPaginationSettings.getDefaultPageSize());
    }

    public static class Builder {
        private int defaultPageSize;

        public Builder defaultPageSize(int defaultPageSize) {
            this.defaultPageSize = defaultPageSize;
            return this;
        }

        public UserPaginationSettings build() {
            return new UserPaginationSettings(defaultPageSize);
        }
    }
}
