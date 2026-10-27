package com.example.tcmproject;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 覆盖“注册 → 登录 → 健康档案 → 问诊记录”的完整闭环 */
@SpringBootTest
@AutoConfigureMockMvc
class ConsultationFlowTests {

    @Autowired
    private MockMvc mockMvc;

    private String uniqueName(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    private String content(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private long extractId(String json) {
        Matcher matcher = Pattern.compile("\"(?:userId|id)\"\\s*:\\s*(\\d+)").matcher(json);
        assertThat(matcher.find()).as("响应中应包含 id: %s", json).isTrue();
        return Long.parseLong(matcher.group(1));
    }

    @Test
    void registerLoginHealthAndRecordsWork() throws Exception {
        String username = uniqueName("alice_");

        // 注册
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\",\"nickname\":\"小艾\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.nickname").value("小艾"));

        // 重复注册被拒绝
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("账号已存在，请直接登录"));

        // 密码错误
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // 正确登录
        MvcResult login = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.nickname").value("小艾"))
                .andReturn();

        long userId = extractId(content(login));
        assertThat(userId).isPositive();

        // 资料接口不能泄露密码
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist());

        // 健康档案：非法身高
        mockMvc.perform(post("/api/users/" + userId + "/health")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"height\":20,\"weight\":65}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // 健康档案：正常保存，BMI 由后端计算
        mockMvc.perform(post("/api/users/" + userId + "/health")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"height\":170,\"weight\":65.2,\"bloodType\":\"O型\",\"geneticHistory\":\"高血压家族史\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.height").value(170.0))
                .andExpect(jsonPath("$.bmi").value(22.6))
                .andExpect(jsonPath("$.bloodType").value("O型"))
                .andExpect(jsonPath("$.geneticHistory").value("高血压家族史"));

        // 重新读取，确认真的落库了
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").value(22.6))
                .andExpect(jsonPath("$.bloodType").value("O型"));

        // 保存问诊记录
        mockMvc.perform(post("/api/records?userId=" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symptoms\":\"头晕乏力、面色苍白\",\"diagnosisType\":\"气血两虚证\"," +
                                "\"features\":\"气虚质兼血虚质\",\"risks\":\"免疫力低下\"," +
                                "\"advice\":\"多食红枣桂圆，练习八段锦\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosisType").value("气血两虚证"))
                .andExpect(jsonPath("$.consultTime").exists());

        // 列表
        MvcResult list = mockMvc.perform(get("/api/records?userId=" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].diagnosisType").value("气血两虚证"))
                .andExpect(jsonPath("$[0].symptoms").value("头晕乏力、面色苍白"))
                .andReturn();

        long recordId = extractId(content(list));

        // 详情（本人）
        mockMvc.perform(get("/api/records/" + recordId + "?userId=" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.advice").value("多食红枣桂圆，练习八段锦"));

        // 缺少证型不允许保存
        mockMvc.perform(post("/api/records?userId=" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symptoms\":\"随便聊聊\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recordsAreIsolatedBetweenUsers() throws Exception {
        String first = uniqueName("u1_");
        String second = uniqueName("u2_");

        long firstId = registerAndGetId(first);
        long secondId = registerAndGetId(second);

        MvcResult created = mockMvc.perform(post("/api/records?userId=" + firstId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symptoms\":\"失眠\",\"diagnosisType\":\"心脾两虚证\",\"features\":\"多梦易醒\"," +
                                "\"risks\":\"气血不足\",\"advice\":\"睡前按揉神门穴\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long recordId = extractId(content(created));

        // 另一个用户看不到这条记录
        mockMvc.perform(get("/api/records/" + recordId + "?userId=" + secondId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/records?userId=" + secondId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private long registerAndGetId(String username) throws Exception {
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk());

        MvcResult login = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(content(login));
    }
}