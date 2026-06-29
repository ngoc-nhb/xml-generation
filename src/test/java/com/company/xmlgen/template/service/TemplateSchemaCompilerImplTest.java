package com.company.xmlgen.template.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.domain.TemplateCompileContext;
import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;
import com.company.xmlgen.template.entity.TemplateStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class TemplateSchemaCompilerImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TemplateSchemaCompiler compiler = new TemplateSchemaCompilerImpl(objectMapper);

    @Test
    void compile_simpleRuntimeTemplate_generatesCompiledJson() {
        RuntimeTemplate runtimeTemplate = new RuntimeTemplate(List.of(field("Game", "Game", 1)));

        JsonNode compiled = compiler.compile(runtimeTemplate, TemplateCompileContext.empty());

        assertThat(compiled.get("roots")).hasSize(1);
        assertThat(compiled.at("/roots/0/fieldName").asText()).isEqualTo("Game");
        assertThat(compiled.at("/roots/0/name").asText()).isEqualTo("Game");
        assertThat(compiled.at("/roots/0/fieldType").asText()).isEqualTo("ELEMENT");
        assertThat(compiled.at("/roots/0/children")).isEmpty();
        assertThat(compiled.get("mappings")).isEmpty();
    }

    @Test
    void compile_nestedHierarchy_preservesJsonHierarchy() {
        RuntimeTemplate runtimeTemplate = new RuntimeTemplate(List.of(
                field("Game", "Game", 1, field("GameId", "GameID", 1))));

        JsonNode compiled = compiler.compile(runtimeTemplate, TemplateCompileContext.empty());

        assertThat(compiled.at("/roots/0/fieldName").asText()).isEqualTo("Game");
        assertThat(compiled.at("/roots/0/children/0/fieldName").asText()).isEqualTo("GameId");
        assertThat(compiled.at("/roots/0/children/0/name").asText()).isEqualTo("GameID");
    }

    @Test
    void compile_mappings_includesMappingMetadata() {
        RuntimeTemplate runtimeTemplate = new RuntimeTemplate(List.of(field("GameKindId", "GameKindID", 1)));
        TemplateCompileContext context = new TemplateCompileContext(List.of(
                new TemplateCompileMapping("GameKindId", "GAME_KIND", "game_kind_id")));

        JsonNode compiled = compiler.compile(runtimeTemplate, context);

        assertThat(compiled.at("/roots/0/masterDataType").asText()).isEqualTo("GAME_KIND");
        assertThat(compiled.at("/roots/0/masterDataField").asText()).isEqualTo("game_kind_id");
        assertThat(compiled.at("/mappings/0/fieldName").asText()).isEqualTo("GameKindId");
        assertThat(compiled.at("/mappings/0/masterDataType").asText()).isEqualTo("GAME_KIND");
        assertThat(compiled.at("/mappings/0/masterDataField").asText()).isEqualTo("game_kind_id");
    }

    @Test
    void compile_repeatedCompilation_isDeterministic() {
        RuntimeTemplate runtimeTemplate = new RuntimeTemplate(List.of(
                field("Second", "Second", 2),
                field("First", "First", 1)));
        TemplateCompileContext context = new TemplateCompileContext(List.of(
                new TemplateCompileMapping("Second", "TYPE_B", "field_b"),
                new TemplateCompileMapping("First", "TYPE_A", "field_a")));

        JsonNode first = compiler.compile(runtimeTemplate, context);
        JsonNode second = compiler.compile(runtimeTemplate, context);

        assertThat(first).isEqualTo(second);
        assertThat(first.toString()).isEqualTo(second.toString());
        assertThat(first.at("/roots/0/fieldName").asText()).isEqualTo("First");
        assertThat(first.at("/mappings/0/fieldName").asText()).isEqualTo("First");
    }

    @Test
    void compile_persistenceCallerCanStoreCompiledSchemaJson() {
        TemplateEntity template = new TemplateEntity("LIVE_GAME", "Live Game", TemplateStatus.ACTIVE, 1L);
        RuntimeTemplate runtimeTemplate = new RuntimeTemplate(List.of(field("Game", "Game", 1)));

        template.setCompiledSchemaJson(compiler.compile(runtimeTemplate, TemplateCompileContext.empty()));

        assertThat(template.getCompiledSchemaJson()).isNotNull();
        assertThat(template.getCompiledSchemaJson().at("/roots/0/fieldName").asText()).isEqualTo("Game");
    }

    private static RuntimeField field(String fieldName, String xmlName, int displayOrder, RuntimeField... children) {
        return new RuntimeField(
                fieldName,
                xmlName,
                fieldName,
                TemplateFieldNodeType.ELEMENT,
                TemplateFieldValueType.STRING,
                TemplateFieldSourceType.INPUT,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                false,
                null,
                null,
                null,
                null,
                null,
                displayOrder,
                null,
                List.of(children));
    }
}
