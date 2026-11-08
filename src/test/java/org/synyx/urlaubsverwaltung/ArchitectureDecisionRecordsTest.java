package org.synyx.urlaubsverwaltung;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Keeps the index of ADRs in the "Decisions" section of {@code AGENTS.md} in sync with {@code docs/adr},
 * see the section "Index" of {@code docs/adr/README.md}.
 */
class ArchitectureDecisionRecordsTest {

    private static final Path AGENTS_MD = Path.of("AGENTS.md");
    private static final Path ADR_DIRECTORY = Path.of("docs/adr");
    private static final String README = "docs/adr/README.md";
    private static final int INDEX_LIMIT = 15;

    private static final Pattern ADR_FILE_NAME = Pattern.compile("\\d{4}-\\d{2}-\\d{2}-[a-z0-9-]+\\.md");
    private static final Pattern STATUS = Pattern.compile("^status: *(?<value>\\S*)", Pattern.MULTILINE);
    private static final Pattern TITLE = Pattern.compile("^# (?<value>.+)$", Pattern.MULTILINE);
    private static final Pattern INDEX_ENTRY = Pattern.compile("(?<link>- (?:\\(proposed\\) )?\\[.+?]\\(.+?\\)):\\s+\\S.*");
    private static final Set<String> STATUSES = Set.of("draft", "proposed", "accepted", "superseded", "expired", "retired");

    @Test
    void ensureAgentsMdIndexesExactlyTheProposedAndAcceptedAdrs() throws IOException {

        final List<String> expectedLinks = new ArrayList<>();
        try (Stream<Path> files = Files.list(ADR_DIRECTORY)) {
            for (Path adr : files.filter(file -> ADR_FILE_NAME.matcher(file.getFileName().toString()).matches()).toList()) {
                final String content = Files.readString(adr);
                final String status = find(STATUS, content);
                assertThat(STATUSES).as("status of %s (see %s)", adr, README).contains(status);
                if (status.equals("proposed") || status.equals("accepted")) {
                    expectedLinks.add("- %s[%s](docs/adr/%s)".formatted(
                        status.equals("proposed") ? "(proposed) " : "", find(TITLE, content), adr.getFileName()));
                }
            }
        }

        final List<String> entries = indexEntries();
        assertThat(entries)
            .as("entries in the \"Decisions\" section of %s: the title of an ADR as link, a colon and when it applies (see %s)", AGENTS_MD, README)
            .allMatch(entry -> INDEX_ENTRY.matcher(entry).matches());
        assertThat(entries)
            .map(entry -> INDEX_ENTRY.matcher(entry).replaceFirst("${link}"))
            .as("Every proposed and accepted ADR, and no other, needs an entry in the \"Decisions\" section of %s (see %s)", AGENTS_MD, README)
            .containsExactlyInAnyOrderElementsOf(expectedLinks);
        assertThat(entries)
            .as("The index in %s has outgrown it, see \"When the index outgrows AGENTS.md\" in %s", AGENTS_MD, README)
            .hasSizeLessThanOrEqualTo(INDEX_LIMIT);
    }

    /**
     * @return the list entries of the "Decisions" section of AGENTS.md, each joined with its indented continuation lines
     */
    private static List<String> indexEntries() throws IOException {
        final List<String> entries = new ArrayList<>();
        boolean inDecisionsSection = false;
        for (String line : Files.readAllLines(AGENTS_MD)) {
            if (line.startsWith("## ")) {
                inDecisionsSection = line.equals("## Decisions");
            } else if (inDecisionsSection && line.startsWith("- ")) {
                entries.add(line.strip());
            } else if (inDecisionsSection && line.startsWith("  ") && !entries.isEmpty()) {
                entries.add(entries.removeLast() + " " + line.strip());
            }
        }
        return entries;
    }

    private static String find(Pattern pattern, String content) {
        final Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group("value").strip() : "";
    }
}
