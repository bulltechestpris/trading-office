package com.trading;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class EnrichedAllocationReportMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(EnrichedAllocationReportMessageListener.class);

    private final JasperReport jasperReport;
    private final Sender<Confirmation> confirmationSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public EnrichedAllocationReportMessageListener(Sender<Confirmation> confirmationSender) throws JRException {
        this.confirmationSender = confirmationSender;
        InputStream resourceAsStream = EnrichedAllocationReportMessageListener.class
                .getClassLoader().getResourceAsStream("Confirmation.jrxml");

        jasperReport = JasperCompileManager.compileReport(resourceAsStream);
    }

    @JmsListener(destination = "outgoing.allocation.report.queue", containerFactory = "jmsContainerFactory")
    public void eventListener(String message) throws IOException {
        AllocationReport allocationReport = objectMapper.readValue(message, AllocationReport.class);
        LOG.info("Received: " + allocationReport);

        try {
            byte[] data = JasperRunManager.runReportToPdf(
                    jasperReport, parameters(allocationReport), new JREmptyDataSource()
            );

            allocationReport.setMessageStatus(MessageStatus.SENT);

            Confirmation confirmation = createConfirmationBasedOn(allocationReport, data);
            confirmationSender.send(confirmation);

        } catch (JRException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Confirmation createConfirmationBasedOn(AllocationReport allocationReport, byte[] data) {
        Confirmation confirmation = new Confirmation();
        confirmation.setAllocationReport(allocationReport);
        confirmation.setContent(data);
        return confirmation;
    }

    private Map<String, Object> parameters(AllocationReport allocationReport) {
        Map<String, Object> map = new HashMap<>();
        map.put("ALLOC_RPT_ID", allocationReport.getAllocationId());
        map.put("TRANS_TYPE", allocationReport.getTransactionType().toString());
        map.put("INST_ID_TYPE", allocationReport.getSecurityIdSource().toString());
        map.put("INST_ID", allocationReport.getSecurityId());

        Instrument instrument = allocationReport.getInstrument();
        map.put("ALLOC_INSTR_NAME", instrument.getName());
        map.put("CURRENCY", instrument.getCurrency());
        map.put("EXCHANGE", instrument.getExchange());

        return map;
    }
}