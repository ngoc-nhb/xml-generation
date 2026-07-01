package com.company.xmlgen.xmlgeneration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.xmlgen.authentication.service.TokenProvider;
import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.ErrorResponseWriter;
import com.company.xmlgen.exception.GlobalExceptionHandler;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.xmlgeneration.dto.ExportValidationError;
import com.company.xmlgen.xmlgeneration.exception.ExportErrorCode;
import com.company.xmlgen.xmlgeneration.service.ExportService;
import com.company.xmlgen.workspace.service.WorkspaceContextResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExportController.class)
@Import({GlobalExceptionHandler.class, ErrorResponseWriter.class})
@AutoConfigureMockMvc(addFilters = false)
class ExportControllerTest {

    private static final Long TEMPLATE_ID = 10L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExportService exportService;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private WorkspaceContextResolver workspaceContextResolver;

    @Test
    void export_success_returnsXmlPayload() throws Exception {
        when(exportService.export(any()))
                .thenReturn(com.company.xmlgen.xmlgeneration.dto.ExportResponse.success("<Game/>"));

        mockMvc.perform(post("/api/v1/templates/{templateId}/export", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inputData": { "GameId": 123 },
                                  "selectedMasterData": {}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.xml").value("<Game/>"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void export_validationFailure_returnsErrorsEnvelope() throws Exception {
        when(exportService.export(any()))
                .thenReturn(com.company.xmlgen.xmlgeneration.dto.ExportResponse.validationFailed(List.of(
                        new ExportValidationError("GameId", "SOURCE_TYPE_REQUIRED", "sourceType is required"))));

        mockMvc.perform(post("/api/v1/templates/{templateId}/export", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field").value("GameId"))
                .andExpect(jsonPath("$.errors[0].code").value("SOURCE_TYPE_REQUIRED"));
    }

    @Test
    void export_templateNotFound_returns404() throws Exception {
        when(exportService.export(any()))
                .thenThrow(new NotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

        mockMvc.perform(post("/api/v1/templates/{templateId}/export", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].code").value("TEMPLATE_NOT_FOUND"));
    }

    @Test
    void export_templateNotCompiled_returns400() throws Exception {
        when(exportService.export(any()))
                .thenThrow(new BusinessException(ExportErrorCode.TEMPLATE_NOT_COMPILED, "Template has not been compiled"));

        mockMvc.perform(post("/api/v1/templates/{templateId}/export", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].code").value("TEMPLATE_NOT_COMPILED"));
    }

    @Test
    void export_emptyInput_delegatesToExportService() throws Exception {
        when(exportService.export(any()))
                .thenReturn(com.company.xmlgen.xmlgeneration.dto.ExportResponse.success("<Game/>"));

        mockMvc.perform(post("/api/v1/templates/{templateId}/export", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<com.company.xmlgen.xmlgeneration.dto.ExportRequest> captor =
                ArgumentCaptor.forClass(com.company.xmlgen.xmlgeneration.dto.ExportRequest.class);
        verify(exportService).export(captor.capture());
        assertThat(captor.getValue().templateId()).isEqualTo(TEMPLATE_ID);
        assertThat(captor.getValue().inputData()).isNull();
        assertThat(captor.getValue().selectedMasterData()).isNull();
    }

    @Test
    void export_missingRequestBody_delegatesWithNullPayload() throws Exception {
        when(exportService.export(any()))
                .thenReturn(com.company.xmlgen.xmlgeneration.dto.ExportResponse.success("<Game/>"));

        mockMvc.perform(post("/api/v1/templates/{templateId}/export", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<com.company.xmlgen.xmlgeneration.dto.ExportRequest> captor =
                ArgumentCaptor.forClass(com.company.xmlgen.xmlgeneration.dto.ExportRequest.class);
        verify(exportService).export(captor.capture());
        assertThat(captor.getValue().templateId()).isEqualTo(TEMPLATE_ID);
        assertThat(captor.getValue().inputData()).isNull();
        assertThat(captor.getValue().selectedMasterData()).isNull();
    }

    @Test
    void export_invalidRequestBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/templates/{templateId}/export", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_FAILED"));
    }

    @Test
    void export_success_matchesApiContract() throws Exception {
        when(exportService.export(any()))
                .thenReturn(com.company.xmlgen.xmlgeneration.dto.ExportResponse.success("<Game><GameID>1</GameID></Game>"));

        String responseBody = mockMvc.perform(post("/api/v1/templates/{templateId}/export", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inputData": { "GameId": 1 },
                                  "selectedMasterData": {}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var response = objectMapper.readTree(responseBody);
        assertThat(response.get("success").asBoolean()).isTrue();
        assertThat(response.has("data")).isTrue();
        assertThat(response.get("data").has("xml")).isTrue();
        assertThat(response.has("errors")).isFalse();
    }
}
