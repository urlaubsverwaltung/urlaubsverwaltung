package org.synyx.urlaubsverwaltung.csv;

import liquibase.util.csv.CSVWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CsvExportServiceTest {

    @Spy
    private CsvExportService<String> sut;

    @Test
    void bom() {
        final byte[] bom = sut.bom();
        assertThat(bom).isEqualTo(new byte[]{(byte) 239, (byte) 187, (byte) 191});
    }

    @Test
    void ensureBomIsUsedAsDefault() {
        final FilterPeriod period = new FilterPeriod(LocalDate.of(2022, 10, 2), LocalDate.of(2022, 10, 3));
        final ByteArrayResource aLotOfData = sut.resource(period, List.of());
        assertThat(aLotOfData.getByteArray()).startsWith((byte) 239, (byte) 187, (byte) 191);
    }

    @Test
    void separator() {
        final char separator = sut.separator();
        assertThat(separator).isEqualTo(';');
    }

    @Test
    void ensureSeparatorIsUsedAsDefault() {

        final CsvExportService<String> sut = new CsvExportService<>() {
            @Override
            public void write(FilterPeriod period, List<String> data, CSVWriter csvWriter) {
                final String[] row = new String[data.size()];
                row[0] = data.get(0);
                row[1] = data.get(1);
                csvWriter.writeNext(row);
            }

            @Override
            public String fileName(FilterPeriod period) {
                return "someFileName.csv";
            }
        };

        final FilterPeriod period = new FilterPeriod(LocalDate.of(2022, 10, 2), LocalDate.of(2022, 10, 3));
        final ByteArrayResource aLotOfData = sut.resource(period, List.of("A lot of data", "Next data"));
        assertThat(new String(aLotOfData.getByteArray(), UTF_8)).contains("A lot of data;Next data");
    }

    @Test
    void ensureResourceWillBeWrittenWithCorrectContent() {

        final CsvExportService<String> sut = new CsvExportService<>() {
            @Override
            public void write(FilterPeriod period, List<String> data, CSVWriter csvWriter) {
                csvWriter.writeNext(new String[]{data.get(0)});

            }

            @Override
            public String fileName(FilterPeriod period) {
                return "someFileName.csv";
            }
        };

        final FilterPeriod period = new FilterPeriod(LocalDate.of(2022, 10, 2), LocalDate.of(2022, 10, 3));
        final ByteArrayResource aLotOfData = sut.resource(period, List.of("A lot of data"));
        assertThat(new String(aLotOfData.getByteArray(), UTF_8)).contains("A lot of data");
    }
}
