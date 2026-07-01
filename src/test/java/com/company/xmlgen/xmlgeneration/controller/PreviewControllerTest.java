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
import com.company.xmlgen.xmlgeneration.dto.PreviewValidationError;
import com.company.xmlgen.xmlgeneration.exception.PreviewErrorCode;
import com.company.xmlgen.xmlgeneration.service.PreviewService;
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

@WebMvcTest(PreviewController.class)
@Import({GlobalExceptionHandler.class, ErrorResponseWriter.class})
@AutoConfigureMockMvc(addFilters = false)
class PreviewControllerTest {

    private static final Long TEMPLATE_ID = 10L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PreviewService previewService;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private WorkspaceContextResolver workspaceContextResolver;

    @Test
    void preview_success_returnsXmlPayload() throws Exception {
        when(previewService.preview(any()))
                .thenReturn(com.company.xmlgen.xmlgeneration.dto.PreviewResponse.success("<Game/>", List.of()));

        mockMvc.perform(post("/api/v1/templates/{templateId}/preview", TEMPLATE_ID)
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
    void preview_validationFailure_returnsErrorsEnvelope() throws Exception {
        when(previewService.preview(any()))
                .thenReturn(com.company.xmlgen.xmlgeneration.dto.PreviewResponse.validationFailed(List.of(
                        new PreviewValidationError("GameId", "SOURCE_TYPE_REQUIRED", "sourceType is required"))));

        mockMvc.perform(post("/api/v1/templates/{templateId}/preview", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors[0].field").value("GameId"))
                .andExpect(jsonPath("$.errors[0].code").value("SOURCE_TYPE_REQUIRED"));
    }

    @Test
    void preview_templateNotFound_returns404() throws Exception {
        when(previewService.preview(any()))
                .thenThrow(new NotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

        mockMvc.perform(post("/api/v1/templates/{templateId}/preview", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].code").value("TEMPLATE_NOT_FOUND"));
    }

    @Test
    void preview_templateNotCompiled_returns400() throws Exception {
        when(previewService.preview(any()))
                .thenThrow(new BusinessException(PreviewErrorCode.TEMPLATE_NOT_COMPILED, "Template has not been compiled"));

        mockMvc.perform(post("/api/v1/templates/{templateId}/preview", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].code").value("TEMPLATE_NOT_COMPILED"));
    }

    @Test
    void preview_emptyInput_delegatesToPreviewService() throws Exception {
        when(previewService.preview(any()))
                .thenReturn(com.company.xmlgen.xmlgeneration.dto.PreviewResponse.success("<Game/>", List.of()));

        mockMvc.perform(post("/api/v1/templates/{templateId}/preview", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<com.company.xmlgen.xmlgeneration.dto.PreviewRequest> captor =
                ArgumentCaptor.forClass(com.company.xmlgen.xmlgeneration.dto.PreviewRequest.class);
        verify(previewService).preview(captor.capture());
        assertThat(captor.getValue().templateId()).isEqualTo(TEMPLATE_ID);
        assertThat(captor.getValue().inputData()).isNull();
        assertThat(captor.getValue().selectedMasterData()).isNull();
    }

    @Test
    void preview_missingRequestBody_delegatesWithNullPayload() throws Exception {
        when(previewService.preview(any()))
                .thenReturn(com.company.xmlgen.xmlgeneration.dto.PreviewResponse.success("<Game/>", List.of()));

        mockMvc.perform(post("/api/v1/templates/{templateId}/preview", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<com.company.xmlgen.xmlgeneration.dto.PreviewRequest> captor =
                ArgumentCaptor.forClass(com.company.xmlgen.xmlgeneration.dto.PreviewRequest.class);
        verify(previewService).preview(captor.capture());
        assertThat(captor.getValue().templateId()).isEqualTo(TEMPLATE_ID);
        assertThat(captor.getValue().inputData()).isNull();
        assertThat(captor.getValue().selectedMasterData()).isNull();
    }

    @Test
    void preview_invalidRequestBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/templates/{templateId}/preview", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_FAILED"));
    }

    @Test
    void preview_success_matchesApiContract() throws Exception {
        when(previewService.preview(any()))
                .thenReturn(com.company.xmlgen.xmlgeneration.dto.PreviewResponse.success("<Game><GameID>1</GameID></Game>", List.of()));

        String responseBody = mockMvc.perform(post("/api/v1/templates/{templateId}/preview", TEMPLATE_ID)
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
