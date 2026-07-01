package com.company.xmlgen.template.importing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.importing.domain.XmlImportNode;
import com.company.xmlgen.template.importing.dto.response.TemplateImportDraftFieldResponse;
import com.company.xmlgen.template.importing.exception.XmlImportErrorCode;
import com.company.xmlgen.template.importing.exception.XmlImportException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XmlImportParserImplTest {

    private XmlImportParser parser;

    @BeforeEach
    void setUp() {
        parser = new XmlImportParserImpl();
    }

    @Test
    void parse_preservesHierarchyAttributesOrderAndValues() {
        String xml =
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <Football>
                  <GameReport>
                    <GameKindID>2</GameKindID>
                    <HomeTeam>Kashima Antlers</HomeTeam>
                    <TeamInfo HV="1">
                      <ID>30676</ID>
                    </TeamInfo>
                  </GameReport>
                </Football>
                """;

        XmlImportNode root = parser.parse(xml.getBytes(StandardCharsets.UTF_8));

        assertThat(root.getNodeName()).isEqualTo("Football");
        assertThat(root.getChildren()).hasSize(1);

        XmlImportNode gameReport = root.getChildren().getFirst();
        assertThat(gameReport.getNodeName()).isEqualTo("GameReport");
        assertThat(gameReport.getChildren()).extracting(XmlImportNode::getNodeName)
                .containsExactly("GameKindID", "HomeTeam", "TeamInfo");

        XmlImportNode gameKindId = gameReport.getChildren().getFirst();
        assertThat(gameKindId.getValue()).isEqualTo("2");

        XmlImportNode teamInfo = gameReport.getChildren().get(2);
        assertThat(teamInfo.getChildren()).extracting(XmlImportNode::getNodeName).containsExactly("HV", "ID");
        assertThat(teamInfo.getChildren().getFirst().getValue()).isEqualTo("1");
    }

    @Test
    void parse_rejectsMalformedXml() {
        assertThatThrownBy(() -> parser.parse("<Game".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(XmlImportException.class)
                .extracting(ex -> ((XmlImportException) ex).getErrorCode())
                .isEqualTo(XmlImportErrorCode.XML_IMPORT_MALFORMED);
    }

    @Test
    void parse_rejectsEmptyXml() {
        assertThatThrownBy(() -> parser.parse("   ".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(XmlImportException.class)
                .extracting(ex -> ((XmlImportException) ex).getErrorCode())
                .isEqualTo(XmlImportErrorCode.XML_IMPORT_EMPTY);
    }
}

class TemplateDraftBuilderImplTest {

    private final TemplateDraftBuilder builder = new TemplateDraftBuilderImpl();
    private final XmlImportParser parser = new XmlImportParserImpl();

    @Test
    void build_populatesDefaultValuesAndMetadata() {
        String xml =
                """
                <Game>
                  <GameKindID>2</GameKindID>
                  <Comment>Hello</Comment>
                </Game>
                """;

        XmlImportNode root = parser.parse(xml.getBytes(StandardCharsets.UTF_8));
        List<TemplateImportDraftFieldResponse> fields = builder.build(root);

        TemplateImportDraftFieldResponse gameKindId = fields.stream()
                .filter(field -> field.xmlName().equals("GameKindID"))
                .findFirst()
                .orElseThrow();
        TemplateImportDraftFieldResponse comment = fields.stream()
                .filter(field -> field.xmlName().equals("Comment"))
                .findFirst()
                .orElseThrow();

        assertThat(gameKindId.nodeType()).isEqualTo(TemplateFieldNodeType.ELEMENT);
        assertThat(gameKindId.defaultValue()).isEqualTo("2");
        assertThat(gameKindId.emptyHandling()).isEqualTo(TemplateFieldEmptyHandling.REQUIRED);
        assertThat(gameKindId.sourceType()).isNull();
        assertThat(gameKindId.imported()).isTrue();

        assertThat(comment.defaultValue()).isEqualTo("Hello");
        assertThat(comment.emptyHandling()).isEqualTo(TemplateFieldEmptyHandling.REQUIRED);
        assertThat(comment.fieldName()).isEqualTo("Comment");
        assertThat(comment.xmlName()).isEqualTo("Comment");
        assertThat(comment.parentFieldName()).isEqualTo("Game");
    }

    @Test
    void build_usesParentPrefixedFieldNamesForAmbiguousXmlNames() {
        String xml =
                """
                <GameReport>
                  <TeamInfo>
                    <ID>1</ID>
                    <GameState ID="1">
                      <Score>0</Score>
                    </GameState>
                  </TeamInfo>
                  <GoalInfo No="1">
                    <StateID>1</StateID>
                  </GoalInfo>
                  <PKInfo>
                    <Player>
                      <No>1</No>
                    </Player>
                  </PKInfo>
                </GameReport>
                """;

        XmlImportNode root = parser.parse(xml.getBytes(StandardCharsets.UTF_8));
        List<TemplateImportDraftFieldResponse> fields = builder.build(root);

        assertThat(fields.stream().filter(field -> "Player".equals(field.parentFieldName())).findFirst())
                .get()
                .satisfies(field -> {
                    assertThat(field.fieldName()).isEqualTo("Player_No");
                    assertThat(field.xmlName()).isEqualTo("No");
                });
        assertThat(fields.stream().filter(field -> "GoalInfo".equals(field.parentFieldName()) && "No".equals(field.xmlName()))
                        .findFirst())
                .get()
                .satisfies(field -> {
                    assertThat(field.fieldName()).isEqualTo("GoalInfo_No");
                    assertThat(field.xmlName()).isEqualTo("No");
                });
        assertThat(fields.stream().filter(field -> "TeamInfo".equals(field.parentFieldName()) && "ID".equals(field.xmlName()))
                        .findFirst())
                .get()
                .satisfies(field -> {
                    assertThat(field.fieldName()).isEqualTo("TeamInfo_ID");
                    assertThat(field.xmlName()).isEqualTo("ID");
                });
        assertThat(fields.stream().filter(field -> "GameReport".equals(field.parentFieldName()) && "TeamInfo".equals(field.xmlName()))
                        .findFirst())
                .get()
                .satisfies(field -> assertThat(field.fieldName()).isEqualTo("TeamInfo"));
    }

    @Test
    void build_liveGameXml_usesStablePrefixedNamesForNoFields() throws Exception {
        byte[] xml = java.nio.file.Files.readAllBytes(java.nio.file.Path.of("test_data/live_game.xml"));
        List<TemplateImportDraftFieldResponse> fields = builder.build(parser.parse(xml));

        assertThat(fields.stream()
                        .filter(field -> "Player".equals(field.parentFieldName()) && "No".equals(field.xmlName()))
                        .findFirst())
                .get()
                .satisfies(field -> {
                    assertThat(field.fieldName()).isEqualTo("Player_No");
                    assertThat(field.xmlName()).isEqualTo("No");
                });
        assertThat(fields.stream()
                        .filter(field -> "GoalInfo".equals(field.parentFieldName()) && "No".equals(field.xmlName()))
                        .findFirst())
                .get()
                .satisfies(field -> {
                    assertThat(field.fieldName()).isEqualTo("GoalInfo_No");
                    assertThat(field.xmlName()).isEqualTo("No");
                });
        assertThat(fields.stream().map(TemplateImportDraftFieldResponse::fieldName))
                .doesNotContain("No_2");
    }

    @Test
    void build_emptyElements_useEmptyTagIfEmptyWithBlankDefaultValue() {
        String xml =
                """
                <Game>
                  <Comment></Comment>
                  <Description/>
                  <Remark></Remark>
                </Game>
                """;

        XmlImportNode root = parser.parse(xml.getBytes(StandardCharsets.UTF_8));
        List<TemplateImportDraftFieldResponse> fields = builder.build(root);

        assertThat(fields.stream().filter(field -> field.xmlName().equals("Comment")).findFirst())
                .get()
                .satisfies(field -> {
                    assertThat(field.defaultValue()).isEqualTo("");
                    assertThat(field.emptyHandling()).isEqualTo(TemplateFieldEmptyHandling.EMPTY_TAG_IF_EMPTY);
                    assertThat(field.sourceType()).isNull();
                });
        assertThat(fields.stream().filter(field -> field.xmlName().equals("Description")).findFirst())
                .get()
                .satisfies(field -> {
                    assertThat(field.defaultValue()).isEqualTo("");
                    assertThat(field.emptyHandling()).isEqualTo(TemplateFieldEmptyHandling.EMPTY_TAG_IF_EMPTY);
                });
        assertThat(fields.stream().filter(field -> field.xmlName().equals("Remark")).findFirst())
                .get()
                .satisfies(field -> {
                    assertThat(field.defaultValue()).isEqualTo("");
                    assertThat(field.emptyHandling()).isEqualTo(TemplateFieldEmptyHandling.EMPTY_TAG_IF_EMPTY);
                });
    }
}

class TemplateImportServiceImplTest {

    private TemplateImportService service;

    @BeforeEach
    void setUp() {
        service = new TemplateImportServiceImpl(new XmlImportParserImpl(), new TemplateDraftBuilderImpl());
    }

    @Test
    void importXml_suggestsNameAndCodeFromFilename() {
        String xml = "<Game><GameKindID>2</GameKindID></Game>";
        var file = new org.springframework.mock.web.MockMultipartFile(
                "file", "live_game.xml", "application/xml", xml.getBytes(StandardCharsets.UTF_8));

        var draft = service.importXml(file);

        assertThat(draft.suggestedName()).isEqualTo("live_game");
        assertThat(draft.suggestedCode()).isEqualTo("LIVE_GAME");
        assertThat(draft.fields()).isNotEmpty();
    }
}
