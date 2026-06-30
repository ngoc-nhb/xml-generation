package com.company.xmlgen.template.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldEntity;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateStatus;
import com.company.xmlgen.template.exception.TemplateSchemaParserErrorCode;
import com.company.xmlgen.template.exception.TemplateSchemaParserException;
import java.util.List;
import org.junit.jupiter.api.Test;

class TemplateSchemaParserImplTest {

    private final TemplateSchemaParser parser = new TemplateSchemaParserImpl();

    @Test
    void parse_simpleHierarchy_buildsRuntimeTree() {
        TemplateFieldEntity root = field(1L, null, "Game", "Game", 1);
        TemplateFieldEntity child = field(2L, 1L, "GameId", "GameID", 1);

        RuntimeTemplate runtime = parser.parse(template(), List.of(child, root));

        assertThat(runtime.roots()).hasSize(1);
        assertThat(runtime.roots().getFirst().fieldName()).isEqualTo("Game");
        assertThat(runtime.roots().getFirst().children()).hasSize(1);
        assertThat(runtime.roots().getFirst().children().getFirst().fieldName()).isEqualTo("GameId");
    }

    @Test
    void parse_nestedHierarchy_attachesChildrenRecursively() {
        TemplateFieldEntity root = field(1L, null, "Game", "Game", 1);
        TemplateFieldEntity child = field(2L, 1L, "GoalInfo", "GoalInfo", 1);
        TemplateFieldEntity grandchild = field(3L, 2L, "PlayerId", "PlayerID", 1);

        RuntimeTemplate runtime = parser.parse(template(), List.of(grandchild, child, root));

        assertThat(runtime.roots()).hasSize(1);
        assertThat(runtime.roots().getFirst().children().getFirst().fieldName()).isEqualTo("GoalInfo");
        assertThat(runtime.roots().getFirst().children().getFirst().children().getFirst().fieldName())
                .isEqualTo("PlayerId");
    }

    @Test
    void parse_missingParent_throwsParserException() {
        TemplateFieldEntity child = field(2L, 1L, "GameId", "GameID", 1);

        assertThatThrownBy(() -> parser.parse(template(), List.of(child)))
                .isInstanceOfSatisfying(TemplateSchemaParserException.class, ex -> assertThat(ex.getParserErrorCode())
                        .isEqualTo(TemplateSchemaParserErrorCode.TEMPLATE_PARENT_FIELD_NOT_FOUND));
    }

    @Test
    void parse_circularHierarchy_throwsParserException() {
        TemplateFieldEntity first = field(1L, 2L, "First", "First", 1);
        TemplateFieldEntity second = field(2L, 1L, "Second", "Second", 1);

        assertThatThrownBy(() -> parser.parse(template(), List.of(first, second)))
                .isInstanceOfSatisfying(TemplateSchemaParserException.class, ex -> assertThat(ex.getParserErrorCode())
                        .isEqualTo(TemplateSchemaParserErrorCode.TEMPLATE_PARENT_CYCLE));
    }

    @Test
    void parse_preservesSiblingDisplayOrder() {
        TemplateFieldEntity root = field(1L, null, "Game", "Game", 1);
        TemplateFieldEntity second = field(2L, 1L, "Second", "Second", 2);
        TemplateFieldEntity first = field(3L, 1L, "First", "First", 1);

        RuntimeTemplate runtime = parser.parse(template(), List.of(second, first, root));

        assertThat(runtime.roots().getFirst().children())
                .extracting("fieldName")
                .containsExactly("First", "Second");
    }

    @Test
    void parse_duplicateFieldName_throwsParserException() {
        TemplateFieldEntity first = field(1L, null, "Game", "Game", 1);
        TemplateFieldEntity duplicate = field(2L, null, "Game", "Game2", 2);

        assertThatThrownBy(() -> parser.parse(template(), List.of(first, duplicate)))
                .isInstanceOfSatisfying(TemplateSchemaParserException.class, ex -> assertThat(ex.getParserErrorCode())
                        .isEqualTo(TemplateSchemaParserErrorCode.TEMPLATE_FIELD_NAME_DUPLICATE));
    }

    @Test
    void parse_parentSavedAsElementWithoutSourceType_normalizesToGroup() {
        TemplateFieldEntity root = containerField(1L, null, "Football", "Football", 1);
        TemplateFieldEntity child = field(2L, 1L, "GameID", "GameID", 1);

        RuntimeTemplate runtime = parser.parse(template(), List.of(child, root));

        assertThat(runtime.roots().getFirst().nodeType()).isEqualTo(TemplateFieldNodeType.GROUP);
        assertThat(runtime.roots().getFirst().sourceType()).isNull();
        assertThat(runtime.roots().getFirst().valueType()).isNull();
    }

    private static TemplateFieldEntity containerField(
            Long id, Long parentId, String fieldName, String xmlName, int displayOrder) {
        TemplateFieldEntity field = mock(TemplateFieldEntity.class);
        when(field.getId()).thenReturn(id);
        when(field.getParentId()).thenReturn(parentId);
        when(field.getFieldName()).thenReturn(fieldName);
        when(field.getXmlName()).thenReturn(xmlName);
        when(field.getDisplayName()).thenReturn(fieldName);
        when(field.getNodeType()).thenReturn(TemplateFieldNodeType.ELEMENT);
        when(field.getSourceType()).thenReturn(null);
        when(field.getValueType()).thenReturn(null);
        when(field.getEmptyHandling()).thenReturn(TemplateFieldEmptyHandling.REQUIRED);
        when(field.getDisplayOrder()).thenReturn(displayOrder);
        return field;
    }

    private static TemplateEntity template() {
        return new TemplateEntity("LIVE_GAME", "Live Game", TemplateStatus.ACTIVE, 1L);
    }

    private static TemplateFieldEntity field(
            Long id, Long parentId, String fieldName, String xmlName, int displayOrder) {
        TemplateFieldEntity field = mock(TemplateFieldEntity.class);
        when(field.getId()).thenReturn(id);
        when(field.getParentId()).thenReturn(parentId);
        when(field.getFieldName()).thenReturn(fieldName);
        when(field.getXmlName()).thenReturn(xmlName);
        when(field.getDisplayName()).thenReturn(fieldName);
        when(field.getNodeType()).thenReturn(TemplateFieldNodeType.ELEMENT);
        when(field.getSourceType()).thenReturn(TemplateFieldSourceType.INPUT);
        when(field.getEmptyHandling()).thenReturn(TemplateFieldEmptyHandling.REQUIRED);
        when(field.getDisplayOrder()).thenReturn(displayOrder);
        return field;
    }

}
