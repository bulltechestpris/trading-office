package com.trading;

import com.google.common.io.Resources;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PdfConfirmationGenerationIntegrationTest {

    @Test
    public void pdf_confirmation_should_be_easily_generated_from_jrxml_template() throws Exception {
        URL jrxmlTemplate = Resources.getResource("Confirmation.jrxml");

        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlTemplate.getFile());

        byte[] data = JasperRunManager.runReportToPdf(jasperReport, parameters(), new JREmptyDataSource());

        assertThat(data.length).isGreaterThan(0);
        Files.write(Paths.get("Test-Confirmation.pdf"), data);
    }

    private Map<String, Object> parameters() {
        Map<String, Object> map = new HashMap<>();
        map.put("ALLOC_RPT_ID", "1234");
        map.put("TRANS_TYPE", "BUY");
        return map;
    }
}
