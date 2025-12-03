package usyd.library_reservation_system.library_reservation_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import usyd.library_reservation_system.library_reservation_system.model.Label;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllLabels() throws Exception {
        mockMvc.perform(get("/api/labels"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetLabelById_Exists() throws Exception {
        mockMvc.perform(get("/api/labels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labelId").value(1));
    }

    @Test
    void testGetLabelById_NotExists() throws Exception {
        mockMvc.perform(get("/api/labels/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetLabelByName_Exists() throws Exception {
        // First create a label to ensure it exists
        String labelName = "Test Label For Name " + System.currentTimeMillis();
        Label label = new Label();
        label.setLabelName(labelName);

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(label)))
                .andExpect(status().isCreated());

        // Now test getting it by name
        mockMvc.perform(get("/api/labels/name/" + labelName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labelName").value(labelName));
    }

    @Test
    void testGetLabelByName_NotExists() throws Exception {
        mockMvc.perform(get("/api/labels/name/NonExistentLabel"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateLabel() throws Exception {
        Label label = new Label();
        label.setLabelName("Test Label " + System.currentTimeMillis());

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(label)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.labelId").exists());
    }

    @Test
    void testCreateLabel_DuplicateName() throws Exception {
        // First create a label
        String uniqueName = "Duplicate Test " + System.currentTimeMillis();
        Label label1 = new Label();
        label1.setLabelName(uniqueName);

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(label1)))
                .andExpect(status().isCreated());

        // Try to create the same label again
        Label label2 = new Label();
        label2.setLabelName(uniqueName);

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(label2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testUpdateLabel_Exists() throws Exception {
        Label label = new Label();
        label.setLabelName("Updated Label " + System.currentTimeMillis());

        mockMvc.perform(put("/api/labels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(label)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labelId").value(1));
    }

    @Test
    void testUpdateLabel_NotExists() throws Exception {
        Label label = new Label();
        label.setLabelName("Non-existent Label");

        mockMvc.perform(put("/api/labels/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(label)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteLabel_NotExists() throws Exception {
        mockMvc.perform(delete("/api/labels/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchLabels() throws Exception {
        mockMvc.perform(get("/api/labels/search")
                        .param("name", "Pro"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testSearchLabels_EmptyResult() throws Exception {
        mockMvc.perform(get("/api/labels/search")
                        .param("name", "NonexistentKeywordxyz123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetLabelsByCreateDate() throws Exception {
        mockMvc.perform(get("/api/labels/ordered/create-date"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetLabelsByName() throws Exception {
        mockMvc.perform(get("/api/labels/ordered/name"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateLabel_WithoutName() throws Exception {
        Label label = new Label();

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(label)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testSearchLabels_WithValidKeyword() throws Exception {
        mockMvc.perform(get("/api/labels/search")
                        .param("name", "Programming"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}

