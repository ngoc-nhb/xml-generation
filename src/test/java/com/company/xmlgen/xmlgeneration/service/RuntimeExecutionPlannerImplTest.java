package com.company.xmlgen.xmlgeneration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;
import com.company.xmlgen.xmlgeneration.domain.ExecutionNode;
import com.company.xmlgen.xmlgeneration.domain.ExecutionPlan;
import com.company.xmlgen.xmlgeneration.exception.RuntimeExecutionPlannerErrorCode;
import com.company.xmlgen.xmlgeneration.exception.RuntimeExecutionPlannerException;
import java.util.List;
import org.junit.jupiter.api.Test;

class RuntimeExecutionPlannerImplTest {

    private final RuntimeExecutionPlanner planner = new RuntimeExecutionPlannerImpl();

    @Test
    void plan_simpleHierarchy_buildsOrderedExecutionNodes() {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(
                field("Game", "Game", TemplateFieldNodeType.GROUP, null, null, 1, field(
                        "GameId",
                        "GameID",
                        TemplateFieldNodeType.ELEMENT,
                        TemplateFieldSourceType.INPUT,
                        null,
                        1))));

        ExecutionPlan plan = planner.plan(runtime);

        assertThat(plan.roots()).hasSize(1);
        assertThat(plan.roots().getFirst().fieldName()).isEqualTo("Game");
        assertThat(plan.roots().getFirst().children()).hasSize(1);
        assertThat(plan.roots().getFirst().children().getFirst().fieldName()).isEqualTo("GameId");
    }

    @Test
    void plan_nestedHierarchy_preservesDeepStructure() {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(field(
                "Game",
                "Game",
                TemplateFieldNodeType.GROUP,
                null,
                null,
                1,
                field(
                        "GoalInfo",
                        "GoalInfo",
                        TemplateFieldNodeType.GROUP,
                        null,
                        null,
                        1,
                        field(
                                "PlayerId",
                                "PlayerID",
                                TemplateFieldNodeType.ELEMENT,
                                TemplateFieldSourceType.INPUT,
                                null,
                                1)))));

        ExecutionPlan plan = planner.plan(runtime);

        ExecutionNode goalInfo = plan.roots().getFirst().children().getFirst();
        assertThat(goalInfo.fieldName()).isEqualTo("GoalInfo");
        assertThat(goalInfo.children().getFirst().fieldName()).isEqualTo("PlayerId");
    }

    @Test
    void plan_repeatableGroup_preservesOccurrenceRule() {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(field(
                "GoalInfo",
                "GoalInfo",
                TemplateFieldNodeType.GROUP,
                null,
                null,
                1,
                field(
                        "PlayerId",
                        "PlayerID",
                        TemplateFieldNodeType.ELEMENT,
                        TemplateFieldSourceType.MASTER_DATA,
                        null,
                        1))));

        ExecutionPlan plan = planner.plan(runtime);

        assertThat(plan.roots().getFirst().occurrenceRule()).isEqualTo(TemplateFieldOccurrenceRule.ZERO_OR_MORE);
        assertThat(plan.roots().getFirst().nodeType()).isEqualTo(TemplateFieldNodeType.GROUP);
    }

    @Test
    void plan_preservesEmptyHandlingAndTriggerActivation() {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(
                field(
                        "Title",
                        "Title",
                        TemplateFieldNodeType.ELEMENT,
                        TemplateFieldSourceType.INPUT,
                        TemplateFieldEmptyHandling.OMIT_IF_EMPTY,
                        1),
                field(
                        "Report",
                        "Report",
                        TemplateFieldNodeType.GROUP,
                        null,
                        TemplateFieldEmptyHandling.REQUIRED,
                        2,
                        true,
                        List.of())));

        ExecutionPlan plan = planner.plan(runtime);

        assertThat(plan.roots()).extracting(ExecutionNode::fieldName).containsExactly("Title", "Report");
        assertThat(plan.roots().getFirst().emptyHandling()).isEqualTo(TemplateFieldEmptyHandling.OMIT_IF_EMPTY);
        assertThat(plan.roots().get(1).triggerActivation()).isTrue();
    }

    @Test
    void plan_repeatedPlanning_isDeterministic() {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(
                field("Second", "Second", TemplateFieldNodeType.ELEMENT, TemplateFieldSourceType.INPUT, null, 2),
                field("First", "First", TemplateFieldNodeType.ELEMENT, TemplateFieldSourceType.INPUT, null, 1)));

        ExecutionPlan first = planner.plan(runtime);
        ExecutionPlan second = planner.plan(runtime);

        assertThat(first).isEqualTo(second);
        assertThat(first.roots()).extracting(ExecutionNode::fieldName).containsExactly("First", "Second");
    }

    @Test
    void plan_nullRuntimeTemplate_throwsPlannerException() {
        assertThatThrownBy(() -> planner.plan(null))
                .isInstanceOfSatisfying(RuntimeExecutionPlannerException.class, ex -> assertThat(ex.getPlannerErrorCode())
                        .isEqualTo(RuntimeExecutionPlannerErrorCode.RUNTIME_TEMPLATE_REQUIRED));
    }

    @Test
    void plan_emptyRuntimeTemplate_returnsEmptyPlan() {
        ExecutionPlan plan = planner.plan(new RuntimeTemplate(List.of()));

        assertThat(plan.roots()).isEmpty();
    }

    @Test
    void plan_preservesExecutionMetadataFromRuntimeField() {
        RuntimeField runtimeField = new RuntimeField(
                "Amount",
                "Amount",
                "Amount Label",
                TemplateFieldNodeType.ELEMENT,
                TemplateFieldValueType.INTEGER,
                TemplateFieldSourceType.STATIC,
                TemplateFieldOccurrenceRule.ZERO_OR_ONE,
                TemplateFieldEmptyHandling.ZERO_IF_EMPTY,
                true,
                false,
                "0",
                "1",
                "/root/amount",
                "ns",
                3,
                "desc",
                List.of());
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(runtimeField));

        ExecutionNode node = planner.plan(runtime).roots().getFirst();

        assertThat(node.fieldName()).isEqualTo("Amount");
        assertThat(node.xmlName()).isEqualTo("Amount");
        assertThat(node.displayName()).isEqualTo("Amount Label");
        assertThat(node.valueType()).isEqualTo(TemplateFieldValueType.INTEGER);
        assertThat(node.sourceType()).isEqualTo(TemplateFieldSourceType.STATIC);
        assertThat(node.occurrenceRule()).isEqualTo(TemplateFieldOccurrenceRule.ZERO_OR_ONE);
        assertThat(node.emptyHandling()).isEqualTo(TemplateFieldEmptyHandling.ZERO_IF_EMPTY);
        assertThat(node.requiredWhenParentExists()).isTrue();
        assertThat(node.triggerActivation()).isFalse();
        assertThat(node.defaultValue()).isEqualTo("0");
        assertThat(node.staticValue()).isEqualTo("1");
        assertThat(node.xmlPath()).isEqualTo("/root/amount");
        assertThat(node.namespace()).isEqualTo("ns");
        assertThat(node.displayOrder()).isEqualTo(3);
        assertThat(node.description()).isEqualTo("desc");
    }

    private static RuntimeField field(
            String fieldName,
            String xmlName,
            TemplateFieldNodeType nodeType,
            TemplateFieldSourceType sourceType,
            TemplateFieldEmptyHandling emptyHandling,
            int displayOrder,
            RuntimeField... children) {
        return field(fieldName, xmlName, nodeType, sourceType, emptyHandling, displayOrder, null, List.of(children));
    }

    private static RuntimeField field(
            String fieldName,
            String xmlName,
            TemplateFieldNodeType nodeType,
            TemplateFieldSourceType sourceType,
            TemplateFieldEmptyHandling emptyHandling,
            int displayOrder,
            Boolean triggerActivation,
            List<RuntimeField> children) {
        return new RuntimeField(
                fieldName,
                xmlName,
                fieldName,
                nodeType,
                null,
                sourceType,
                nodeType == TemplateFieldNodeType.GROUP ? TemplateFieldOccurrenceRule.ZERO_OR_MORE : null,
                emptyHandling != null ? emptyHandling : TemplateFieldEmptyHandling.REQUIRED,
                false,
                triggerActivation,
                null,
                null,
                null,
                null,
                displayOrder,
                null,
                children);
    }
}
