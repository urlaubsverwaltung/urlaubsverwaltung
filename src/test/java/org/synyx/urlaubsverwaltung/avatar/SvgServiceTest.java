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
        final String svg = sut.createSvg("thymeleaf/svg/avatar", Locale.GERMAN, Map.of("initials", "TB"));
        assertThat(svg).isEqualTo("<svg\n" +
            "  class=\"tw-tracking-widest\"\n" +
            "  width=\"42\"\n" +
            "  height=\"42\"\n" +
            "  viewBox=\"0 0 64 64\"\n" +
            "  xmlns=\"http://www.w3.org/2000/svg\"\n" +
            "  role=\"img\"\n" +
            "  focusable=\"false\"\n" +
            "  preserveAspectRatio=\"xMidYMid meet\"\n" +
            ">\n" +
            "  <circle cx=\"32\" cy=\"32\" r=\"32\" fill=\"#eff6ff\" class=\"tw-fill-current\" />\n" +
            "  <text\n" +
            "    x=\"32\"\n" +
            "    y=\"32\"\n" +
            "    text-anchor=\"middle\"\n" +
            "    dominant-baseline=\"central\"\n" +
            "    font-size=\"1.625rem\"\n" +
            "    stroke=\"#27272a\"\n" +
            "    fill=\"#27272a\"\n" +
            "    class=\"tw-stroke-zinc-700 tw-fill-zinc-700 dark:tw-stroke-zinc-100 dark:tw-fill-zinc-100\"\n" +
            "  >TB</text>\n" +
            "</svg>\n");
    }
}
