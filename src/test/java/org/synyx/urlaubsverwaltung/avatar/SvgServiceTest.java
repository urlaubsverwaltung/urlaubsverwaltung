package org.synyx.urlaubsverwaltung.avatar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SvgServiceTest {

    private SvgService sut;

    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        sut = new SvgService(messageSource);
    }

    @Test
    void ensuresToProcessSvgTemplate() {
        final String svg = sut.createSvg("svg/avatar", Locale.GERMAN, Map.of("initials", "TB"));
        assertThat(svg).isEqualTo("""
            <svg
              class="tw-tracking-widest"
              width="42"
              height="42"
              viewBox="0 0 64 64"
              xmlns="http://www.w3.org/2000/svg"
              role="img"
              focusable="false"
              preserveAspectRatio="xMidYMid meet"
            >
              <circle cx="32" cy="32" r="32" fill="#eff6ff" class="tw-fill-current" />
              <text
                x="32"
                y="32"
                text-anchor="middle"
                dominant-baseline="central"
                font-size="1.625rem"
                stroke="#27272a"
                fill="#27272a"
                class="tw-stroke-zinc-700 tw-fill-zinc-700 dark:tw-stroke-zinc-100 dark:tw-fill-zinc-100"
              >TB</text>
            </svg>
            """);
    }
}
