package com.trading;

import com.google.common.io.Resources;
import net.sf.jasperreports.engine.*;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.TransactionalEvent;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.notify.NotifyType;
import org.openspaces.events.polling.Polling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
@EventDriven
@Polling
@NotifyType(write = true, update = true)
@TransactionalEvent
public class ReceivedAllocationReportListener {

    private static final Logger LOG = LoggerFactory.getLogger(ReceivedAllocationReportListener.class);

    private final JasperReport jasperReport;

    public ReceivedAllocationReportListener() throws JRException {
        URL jrxmlTemplate = Resources.getResource("Confirmation.jrxml");

        jasperReport = JasperCompileManager.compileReport(jrxmlTemplate.getFile());
    }

    @EventTemplate
    AllocationReport unprocessedData() {
        AllocationReport template = new AllocationReport();
        template.setMessageStatus(MessageStatus.NEW);
        return template;
    }

    @SpaceDataEvent
    public void eventListener(AllocationReport allocationReport, GigaSpace space) {
        LOG.info("Retrieved from cache: " + allocationReport);

        try {
            byte[] data = JasperRunManager.runReportToPdf(
                    jasperReport, parameters(allocationReport), new JREmptyDataSource()
            );
            Path confirmationpath = Files.write(Paths.get("Confirmation.pdf"), data);
            LOG.info("Confirmation PDF saved: " + confirmationpath.toAbsolutePath().toString());

            allocationReport.setMessageStatus(MessageStatus.SENT);
            space.write(allocationReport);
        } catch (JRException | IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Map<String, Object> parameters(AllocationReport allocationReport) {
        Map<String, Object> map = new HashMap<>();
        map.put("ALLOC_RPT_ID", allocationReport.getAllocationId());
        map.put("TRANS_TYPE", allocationReport.getTransactionType().toString());
        return map;
    }
}
