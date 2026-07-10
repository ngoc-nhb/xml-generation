/**
 * Investigation-only script. Traces frontend checkpoints with actual data.
 * Run: node frontend/scripts/e2e-investigation.mjs
 *
 * Uses the same sampleInputJson shape produced by TemplateImportSampleInputBuilder.
 */

const sampleInputJson = {
    GameSchedule: {
        SendDate: '20260101',
        Year: '2026',
        GameKindID: '2',
        GameKindName: 'J1',
        GameCategory: [
            { SeasonID: '10', SeasonName: 'Spring', CupName: 'Cup A', GameC: 'G1' },
            { SeasonID: '11', SeasonName: 'Summer', CupName: 'Cup B', GameC: 'G2' },
            { SeasonID: '12', SeasonName: 'Fall', CupName: 'Cup C', GameC: 'G3' },
        ],
    },
    ScheduleInfo: {
        Schedule: [
            { ScheduleNo: '1', GameID: 'G1' },
            { ScheduleNo: '2', GameID: 'G2' },
            { ScheduleNo: '3', GameID: 'G3' },
            { ScheduleNo: '4', GameID: 'G4' },
            { ScheduleNo: '5', GameID: 'G5' },
        ],
    },
};

// Minimal schema mirroring import draft (sourceType null until user edits in schema editor)
const schemaFields = [
    { fieldName: 'Football', parentFieldName: null, nodeType: 'GROUP', sourceType: null, occurrenceRule: 'ONE_OR_MORE', emptyHandling: 'REQUIRED', displayOrder: 1, valueType: null, defaultValue: null },
    { fieldName: 'GameSchedule', parentFieldName: 'Football', nodeType: 'GROUP', sourceType: null, occurrenceRule: null, emptyHandling: 'REQUIRED', displayOrder: 1, valueType: null, defaultValue: null },
    { fieldName: 'SendDate', parentFieldName: 'GameSchedule', nodeType: 'ELEMENT', sourceType: 'INPUT', occurrenceRule: null, emptyHandling: 'REQUIRED', displayOrder: 1, valueType: 'STRING', defaultValue: null },
    { fieldName: 'GameCategory', parentFieldName: 'GameSchedule', nodeType: 'GROUP', sourceType: null, occurrenceRule: 'ONE_OR_MORE', emptyHandling: 'REQUIRED', displayOrder: 2, valueType: null, defaultValue: null },
    { fieldName: 'SeasonID', parentFieldName: 'GameCategory', nodeType: 'ELEMENT', sourceType: 'MASTER_DATA', occurrenceRule: null, emptyHandling: 'REQUIRED', displayOrder: 1, valueType: 'STRING', defaultValue: null },
    { fieldName: 'SeasonName', parentFieldName: 'GameCategory', nodeType: 'ELEMENT', sourceType: 'MASTER_DATA', occurrenceRule: null, emptyHandling: 'REQUIRED', displayOrder: 2, valueType: 'STRING', defaultValue: null },
    { fieldName: 'ScheduleInfo', parentFieldName: 'Football', nodeType: 'GROUP', sourceType: null, occurrenceRule: null, emptyHandling: 'REQUIRED', displayOrder: 2, valueType: null, defaultValue: null },
    { fieldName: 'Schedule', parentFieldName: 'ScheduleInfo', nodeType: 'GROUP', sourceType: null, occurrenceRule: 'ONE_OR_MORE', emptyHandling: 'REQUIRED', displayOrder: 1, valueType: null, defaultValue: null },
    { fieldName: 'ScheduleNo', parentFieldName: 'Schedule', nodeType: 'ATTRIBUTE', sourceType: 'INPUT', occurrenceRule: null, emptyHandling: 'REQUIRED', displayOrder: 1, valueType: 'STRING', defaultValue: null },
    { fieldName: 'GameID', parentFieldName: 'Schedule', nodeType: 'ELEMENT', sourceType: 'INPUT', occurrenceRule: null, emptyHandling: 'REQUIRED', displayOrder: 2, valueType: 'STRING', defaultValue: null },
];

function isRepeatable(rule) {
    return rule === 'ZERO_OR_MORE' || rule === 'ONE_OR_MORE';
}

function isScalarDataField(field) {
    return (field.nodeType === 'ELEMENT' || field.nodeType === 'ATTRIBUTE')
        && (field.sourceType === 'INPUT' || field.sourceType === 'MASTER_DATA');
}

function buildFieldTree(fields) {
    const byParent = new Map();
    for (const field of fields) {
        const key = field.parentFieldName ?? null;
        if (!byParent.has(key)) byParent.set(key, []);
        byParent.get(key).push(field);
    }
    for (const siblings of byParent.values()) {
        siblings.sort((a, b) => a.displayOrder - b.displayOrder);
    }
    function build(parent) {
        return (byParent.get(parent) ?? []).map((field) => ({
            field,
            children: build(field.fieldName),
        }));
    }
    return build(null);
}

function fieldHasChildren(field, fields) {
    return fields.some((f) => f.parentFieldName === field.fieldName);
}

function isSchemaContainerField(field, fields) {
    return field.nodeType === 'GROUP' || fieldHasChildren(field, fields);
}

function isContainerNode(node) {
    return node.field.nodeType === 'GROUP' || node.children.length > 0;
}

function unwrapRootInputScope(fields, inputData) {
    const tree = buildFieldTree(fields);
    if (tree.length !== 1) return inputData;
    const root = tree[0];
    if (!isSchemaContainerField(root.field, fields)) return inputData;
    if (root.children.some((child) => child.field.fieldName in inputData)) return inputData;
    const wrapped = inputData[root.field.fieldName];
    if (wrapped !== null && typeof wrapped === 'object' && !Array.isArray(wrapped)) return wrapped;
    return inputData;
}

function normalizeRepeatableWriteItems(value) {
    if (Array.isArray(value)) {
        return value.filter((item) => typeof item === 'object' && item !== null && !Array.isArray(item));
    }
    if (value !== null && typeof value === 'object') return [value];
    return [];
}

function readScalarValue(raw) {
    if (raw === null || raw === undefined) return '';
    if (typeof raw === 'boolean' || typeof raw === 'number') return raw;
    return String(raw);
}

function readNodeInput(node, value) {
    const { field, children } = node;
    if (isContainerNode(node)) {
        if (isRepeatable(field.occurrenceRule)) {
            return normalizeRepeatableWriteItems(value).map((item) => readGroupObject(children, item));
        }
        if (value === null || typeof value !== 'object' || Array.isArray(value)) return {};
        return readGroupObject(children, value);
    }
    if (!isScalarDataField(field)) return undefined;
    return readScalarValue(value);
}

function readGroupObject(children, value) {
    const result = {};
    for (const child of children) {
        const childValue = readNodeInput(child, value[child.field.fieldName]);
        if (childValue !== undefined) result[child.field.fieldName] = childValue;
    }
    return result;
}

function inputDataToFormData(fields, inputData) {
    const tree = buildFieldTree(fields);
    const inputScope = unwrapRootInputScope(fields, inputData);
    const result = {};
    for (const root of tree) {
        if (isSchemaContainerField(root.field, fields)) {
            for (const child of root.children) {
                const value = readNodeInput(child, inputScope[child.field.fieldName]);
                if (value !== undefined) result[child.field.fieldName] = value;
            }
            continue;
        }
        const value = readNodeInput(root, inputScope[root.field.fieldName]);
        if (value !== undefined) result[root.field.fieldName] = value;
    }
    return result;
}

function resolveInitialFormData(fields, { savedInputData, importedBaseData }) {
    if (savedInputData) return inputDataToFormData(fields, savedInputData);
    if (importedBaseData && Object.keys(importedBaseData).length > 0) {
        return inputDataToFormData(fields, importedBaseData);
    }
    return {};
}

function writeNodeOutput(node, value) {
    const { field, children } = node;
    if (isContainerNode(node)) {
        if (isRepeatable(field.occurrenceRule)) {
            return normalizeRepeatableWriteItems(value).map((item) => writeGroupObject(children, item));
        }
        if (!value || typeof value !== 'object' || Array.isArray(value)) return {};
        return writeGroupObject(children, value);
    }
    if (!isScalarDataField(field)) return undefined;
    return value ?? '';
}

function writeGroupObject(children, value) {
    const result = {};
    for (const child of children) {
        const output = writeNodeOutput(child, value[child.field.fieldName]);
        if (output !== undefined) result[child.field.fieldName] = output;
    }
    return result;
}

function formDataToInputData(fields, formData) {
    const tree = buildFieldTree(fields);
    const result = {};
    for (const root of tree) {
        if (isSchemaContainerField(root.field, fields)) {
            for (const child of root.children) {
                const output = writeNodeOutput(child, formData[child.field.fieldName]);
                if (output !== undefined) result[child.field.fieldName] = output;
            }
            continue;
        }
        const output = writeNodeOutput(root, formData[root.field.fieldName]);
        if (output !== undefined) result[root.field.fieldName] = output;
    }
    return result;
}

function countInputFields(fields) {
    return fields.filter(isScalarDataField).length;
}

function normalizeRepeatableItems(node, raw) {
    const items = Array.isArray(raw)
        ? raw.filter((item) => typeof item === 'object' && item !== null && !Array.isArray(item))
        : [];
    if (node.field.occurrenceRule === 'ONE_OR_MORE' && items.length === 0) {
        return [{}];
    }
    return items;
}

console.log('CHECKPOINT: Frontend receives sampleInputJson (simulated API response)');
console.log('  Status: PASS');
console.log('  Schedule array size:', sampleInputJson.ScheduleInfo?.Schedule?.length ?? 0);
console.log('  GameCategory array size:', sampleInputJson.GameSchedule.GameCategory.length);
console.log('  SeasonID in sample:', sampleInputJson.GameSchedule.GameCategory[0].SeasonID);
console.log('  FULL sampleInputJson:', JSON.stringify(sampleInputJson, null, 2));

const resolvedForm = resolveInitialFormData(schemaFields, { importedBaseData: sampleInputJson });
console.log('\nCHECKPOINT: resolveInitialFormData() — fresh import, no Saved Input');
console.log('  Status: PASS');
console.log('  Schedule form array size:', resolvedForm.ScheduleInfo?.Schedule?.length ?? 0);
console.log('  GameCategory form array size:', resolvedForm.GameSchedule?.GameCategory?.length ?? 0);
console.log('  SeasonID in form:', resolvedForm.GameSchedule?.GameCategory?.[0]?.SeasonID ?? 'MISSING');
console.log('  FULL form object:', JSON.stringify(resolvedForm, null, 2));

const savedInputBroken = { GameSchedule: [] };
const savedResolvedForm = resolveInitialFormData(schemaFields, { savedInputData: savedInputBroken, importedBaseData: sampleInputJson });
console.log('\nCHECKPOINT: resolveInitialFormData() — WITH Saved Input { GameSchedule: [] }');
console.log('  Status: FAIL (imported data bypassed)');
console.log('  Schedule form array size:', savedResolvedForm.Schedule?.length ?? 0);
console.log('  GameSchedule in form:', JSON.stringify(savedResolvedForm.GameSchedule));
console.log('  FULL form object:', JSON.stringify(savedResolvedForm, null, 2));

const previewPayload = {
    inputData: formDataToInputData(schemaFields, savedResolvedForm),
    selectedMasterData: {},
};
console.log('\nCHECKPOINT: Preview Request Payload (after Saved Input override)');
console.log('  Status: FAIL');
console.log('  inputData:', JSON.stringify(previewPayload.inputData, null, 2));

const freshPreviewPayload = {
    inputData: formDataToInputData(schemaFields, resolvedForm),
    selectedMasterData: {},
};
console.log('\nCHECKPOINT: Preview Request Payload (fresh import path)');
console.log('  Status: PASS');
console.log('  Schedule array size:', freshPreviewPayload.inputData.ScheduleInfo?.Schedule?.length ?? 0);
console.log('  GameCategory array size:', freshPreviewPayload.inputData.GameSchedule?.GameCategory?.length ?? 0);
console.log('  inputData:', JSON.stringify(freshPreviewPayload.inputData, null, 2));

// Simulate DynamicInputForm repeatable display
const scheduleNode = buildFieldTree(schemaFields).find((r) => r.field.fieldName === 'Football')?.children.find((c) => c.field.fieldName === 'ScheduleInfo')?.children.find((c) => c.field.fieldName === 'Schedule');
const uiItemsFresh = normalizeRepeatableItems(scheduleNode, resolvedForm.ScheduleInfo?.Schedule);
const uiItemsSaved = normalizeRepeatableItems(scheduleNode, savedResolvedForm.ScheduleInfo?.Schedule);
console.log('\nCHECKPOINT: React Form State — Schedule UI item count');
console.log('  Fresh import UI Schedule items:', uiItemsFresh.length);
console.log('  Saved Input override UI Schedule items:', uiItemsSaved.length);

console.log('\nCHECKPOINT: INPUT-only form field count (pre-7.2.2 behavior simulation)');
const inputOnlyCount = schemaFields.filter((f) => (f.nodeType === 'ELEMENT' || f.nodeType === 'ATTRIBUTE') && f.sourceType === 'INPUT').length;
const scalarCount = countInputFields(schemaFields);
console.log('  INPUT-only fields shown:', inputOnlyCount, '(SeasonID/SeasonName excluded:', inputOnlyCount < scalarCount, ')');
