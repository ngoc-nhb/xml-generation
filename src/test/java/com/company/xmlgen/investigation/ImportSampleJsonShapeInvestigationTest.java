package com.company.xmlgen.investigation;

import com.company.xmlgen.template.importing.domain.XmlImportNode;
import com.company.xmlgen.template.importing.dto.response.TemplateImportDraftFieldResponse;
import com.company.xmlgen.template.importing.service.TemplateDraftBuilderImpl;
import com.company.xmlgen.template.importing.service.TemplateImportSampleInputBuilder;
import com.company.xmlgen.template.importing.service.XmlImportParserImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Prints actual sampleInputJson from import pipeline (no Spring/Docker required). */
class ImportSampleJsonShapeInvestigationTest {

    private static final String FOOTBALL_XML =
            """
            <Football>
              <GameSchedule>
                <SendDate>20260101</SendDate>
                <GameCategory>
                  <SeasonID>10</SeasonID>
                  <SeasonName>Spring</SeasonName>
                </GameCategory>
                <GameCategory>
                  <SeasonID>11</SeasonID>
                  <SeasonName>Summer</SeasonName>
                </GameCategory>
              </GameSchedule>
              <ScheduleInfo>
                <Schedule ScheduleNo="1"><GameID>G1</GameID></Schedule>
                <Schedule ScheduleNo="2"><GameID>G2</GameID></Schedule>
                <Schedule ScheduleNo="3"><GameID>G3</GameID></Schedule>
                <Schedule ScheduleNo="4"><GameID>G4</GameID></Schedule>
                <Schedule ScheduleNo="5"><GameID>G5</GameID></Schedule>
              </ScheduleInfo>
            </Football>
            """;

    @Test
    void printActualSampleInputJsonShape() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        XmlImportParserImpl parser = new XmlImportParserImpl();
        TemplateDraftBuilderImpl draftBuilder = new TemplateDraftBuilderImpl();
        TemplateImportSampleInputBuilder sampleBuilder = new TemplateImportSampleInputBuilder(mapper);

        XmlImportNode root = parser.parse(FOOTBALL_XML.getBytes(StandardCharsets.UTF_8));
        List<TemplateImportDraftFieldResponse> fields = draftBuilder.build(root);
        var sample = sampleBuilder.build(root, fields);

        System.out.println("ACTUAL sampleInputJson from TemplateImportSampleInputBuilder:");
        System.out.println(mapper.writeValueAsString(sample));
        System.out.println("Top-level keys: " + sample.fieldNames());
        System.out.println("Has root-level Schedule key: " + sample.has("Schedule"));
        System.out.println("Has ScheduleInfo.Schedule: " + (sample.has("ScheduleInfo") && sample.get("ScheduleInfo").has("Schedule")));
        if (sample.has("ScheduleInfo") && sample.get("ScheduleInfo").has("Schedule")) {
            System.out.println("ScheduleInfo.Schedule array size: " + sample.get("ScheduleInfo").get("Schedule").size());
        }
        if (sample.has("GameSchedule") && sample.get("GameSchedule").has("GameCategory")) {
            System.out.println("GameSchedule.GameCategory array size: "
                    + sample.get("GameSchedule").get("GameCategory").size());
            System.out.println("GameSchedule.GameCategory[0].SeasonID: "
                    + sample.get("GameSchedule").get("GameCategory").get(0).get("SeasonID").asText());
        }
    }
}
