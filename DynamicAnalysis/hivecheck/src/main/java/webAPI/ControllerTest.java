package webAPI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class ControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new Controller()).build();
    }

    @Test
    public void join_check1() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/join_check")
                // set return data type utf-8， default is ISO-8859-1
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .param("t1_name", "mrtest_50")
                .param("t1_key", "city")
                .param("t2_name", "mrtest_10")
                .param("t2_key", "city"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendReduceNum").isNotEmpty())
                .andExpect(jsonPath("$.dataImbalancedSuggest").isEmpty());
    }

    @Test
    public void join_check2() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/join_check")
                // set return data type utf-8， default is ISO-8859-1
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .param("t1_name", "mrtest_70kskew")
                .param("t1_key", "loc")
                .param("t2_name", "mrtest_70kskew")
                .param("t2_key", "loc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendReduceNum").isNotEmpty())
                .andExpect(jsonPath("$.dataImbalancedSuggest").isNotEmpty());
    }
}