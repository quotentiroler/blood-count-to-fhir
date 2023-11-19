package com.api;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@WebMvcTest(controllers = BloodController.class)
public class Tests {

    @Autowired
    MockMvc mockMvc;

    /*
     * This test is a simple test to ensure that the conversion from unstructured
     * String to FHIR is working as expected. It is not a comprehensive test, and it
     * might fail sometimes due to the use of GPT.
     */
    @WithMockUser(value = "user")
    @Test
    public void test() throws Exception {
        String actualOutput = mockMvc.perform(MockMvcRequestBuilders.get("/test")).andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertNotEquals(actualOutput, "{}", true);
    }

}
