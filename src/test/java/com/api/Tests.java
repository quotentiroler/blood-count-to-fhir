package com.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.api.util.DataGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest(controllers = BloodController.class)
class Tests {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    BloodDetails bloodDetails;

    List<Observation> observations;

    String json;

    /*
     * Set up the mock mvc and get the response from the controller
     * Convert the response to a BloodDetails object
     */
    @WithMockUser(value = "user")
    @BeforeAll
    void setUp() throws Exception {
        String command = "Please convert this: "
                + Files.readString(
                        AppUtils.resolveResourcePath("testinput.txt"))
                + "  into BloodDetails object with attributes:" +
                Files.readString(
                        AppUtils.resolveResourcePath("command.txt"))
                + " formatted as valid json in the right order.";
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        json = mockMvc.perform(MockMvcRequestBuilders.get("/chat").content(command))
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();
        json = AppUtils.fixJson(json);
        bloodDetails = new ObjectMapper().readValue(json, BloodDetails.class);
        observations = bloodDetails.toObservations();

    }

    /*
     * Test that the observations list is not empty
     */
    @Test
    void test1() throws Exception {
        assertTrue(observations.size() > 0);
    }

    /*
     * Test that the observations list is greater than 10
     */
    @Test
    void test2() throws Exception {
        assertTrue(observations.size() > 10);
    }

    @Test
    void test3() throws Exception {
        // assertFalse(observations.size() > 30);
    }

    @Test
    void test4() throws Exception {
    }

    @AfterAll
    void tearDown() {
        System.out.println("Mapped " + observations.size() + " Observations:");
        for (Observation o : observations) {
            System.out.println(o.getCode().getCodingFirstRep().getDisplay() + ": " + o.getValueQuantity().getValue()
                    + " " + o.getValueQuantity().getUnit());
        }
    }

}
