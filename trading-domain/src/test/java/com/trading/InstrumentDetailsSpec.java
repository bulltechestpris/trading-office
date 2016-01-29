package com.trading;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentDetailsSpec {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void can_be_parsed_to_json_and_converted_back_to_object() throws Exception {

        String instrumentDetailsAsJson = objectMapper.writeValueAsString(
                instrumentDetails()
        );

        InstrumentDetails instrumentDetailsFromJson = objectMapper.readValue(
                instrumentDetailsAsJson, InstrumentDetails.class
        );

        assertThat(instrumentDetailsFromJson).isEqualToComparingFieldByField(instrumentDetails());

        System.out.println(instrumentDetailsFromJson);
    }

    private InstrumentDetails instrumentDetails() {

        InstrumentDetails instrumentDetails = new InstrumentDetails();

        instrumentDetails.setName("DUMMY_NAME");
        instrumentDetails.setTicker("DUMMY_SYMBOL");
        instrumentDetails.setSecurityType("Common Stock");

        return instrumentDetails;
    }
}
