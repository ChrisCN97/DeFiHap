package webAPI;

import org.hamcrest.Matchers;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class staticCheckControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new staticCheckController()).build();
    }

    @Test
    public void astCheck_Post1() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/astCheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"hiveql\": \"select c from t1 where c>1;\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fixedSuggestions").value(Matchers.containsInAnyOrder("Correct HQL.")))
                .andExpect(jsonPath("$.fixedHiveql").doesNotExist());
    }

    @Test
    public void astCheck_Post2() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/astCheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"hiveql\": \"select t1.col1, t2.col2 from table1 as t1 join (select t3.col3,t4.col2 from t3 join t4 on t3.id = t4.id + 100) as t2 on t1.col1  = t2.col2;\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fixedSuggestions").value(Matchers.containsInAnyOrder("Do not calculate in 'join'.", "Do not use too many 'Join' clauses.")))
                .andExpect(jsonPath("$.fixedHiveql").doesNotExist());
    }

    @Test
    public void astCheck_Post3() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/astCheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"hiveql\": \"select date_sub('2020-9-16', interval 10 days) from a;\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fixedSuggestions").value(Matchers.containsInAnyOrder("Be careful! Using \"interval\" in \"date_sub()\" will cause error!")))
                .andExpect(jsonPath("$.fixedHiveql").value("select date_sub('2020-9-16',10) from a"));
    }

    @Test
    public void astCheck_Post4() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/astCheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"hiveql\": \"select t1.name,avg(t1.score),t1.age from t1 group by t1.name;\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fixedSuggestions").value(Matchers.containsInAnyOrder("Warning! Column selected should be included in group by")))
                .andExpect(jsonPath("$.fixedHiveql").value("select t1.name,avg(t1.score),t1.age from t1 group by t1.name,t1.age"));
    }

    @Test
    public void astCheck_Post5() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/astCheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"hiveql\": \"select * from t1;\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fixedSuggestions").value(Matchers.containsInAnyOrder("Be careful! Using \"select *\" will cause poor performance! Please select specific column.")))
                .andExpect(jsonPath("$.fixedHiveql").value("select column1,...,columnn from t1"));
    }

    @Test
    public void astCheck_Post6() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/astCheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"hiveql\": \"select name from partitiontable where name='cn';\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fixedSuggestions").value(Matchers.containsInAnyOrder("Warning! Please utilize partition in the query. Or check database connection.")))
                .andExpect(jsonPath("$.fixedHiveql").value("select name from partitiontable where name='cn' and city? and provice?"));
    }

    @Test
    public void astCheck_Post7() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/astCheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"hiveql\": \"Select mrtest_50.name \\nFrom mrtest_50 \\njoin mrtest_10 t2 on mrtest_50.city = t2.city;\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fixedSuggestions").value(Matchers.containsInAnyOrder("Please put the table containing less records on the left side of join. Or check database connection.")))
                .andExpect(jsonPath("$.joinParams").value(Matchers.contains("mrtest_50","city","mrtest_10","city")))
                .andExpect(jsonPath("$.fixedHiveql").value("select mrtest_50.name from mrtest_10 as t2 inner join mrtest_50 on mrtest_50.city=t2.city"));
    }

    @Test
    public void astCheck_Post8() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/astCheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"hiveql\": \"select t1.name from mrtest_70kskew t1 join mrtest_70kskew t2 on t1.loc = t2.loc\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fixedSuggestions").value(Matchers.containsInAnyOrder("Correct HQL.")))
                .andExpect(jsonPath("$.fixedHiveql").doesNotExist())
                .andExpect(jsonPath("$.joinParams").value(Matchers.contains("mrtest_70kskew","loc","mrtest_70kskew","loc")));
    }

    @Test
    public void astCheck_Post9() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/astCheck")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"hiveql\": \"sect c from t1 where c>1;\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fixedSuggestions").value(Matchers.containsInAnyOrder("This HiveQL may be illegal, please check your input or the database connection.")))
                .andExpect(jsonPath("$.fixedHiveql").doesNotExist());
    }

//    @Test
//    public void configCheck() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.get("/configCheck")
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").value(Matchers.containsInAnyOrder("Task has been rejected by ExecutorService, 应设置hive.server2.thrift.max.worker.threads=1, hive.server2.thrift.min.worker.threads=1","对分区表进行insert未设置动态分区, 应设置hive.exec.dynamic.partition.mode=nonstrict","未启用发生数据倾斜时自动进行负载均衡, 应设置hive.groupby.skewindata=true")));
//    }
}