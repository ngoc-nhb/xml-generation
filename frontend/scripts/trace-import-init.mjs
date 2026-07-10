/**
 * Traces frontend initialization for imported base data scenarios.
 * Run: node scripts/trace-import-init.mjs
 */
import { readFileSync } from 'node:fs';
import { pathToFileURL } from 'node:url';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const distUtils = join(__dirname, '../src/features/xml-generation/utils/inputFormSchema.ts');

// Inline minimal reproduction of the pipeline using dynamic import via ts - not available.
// Use compiled logic by re-implementing key checks from source.

const scheduleFields = [
    { fieldName: 'ScheduleInfo', parentFieldName: null, nodeType: 'GROUP', sourceType: null, occurrenceRule: null, emptyHandling: 'REQUIRED', displayOrder: 1, valueType: null, defaultValue: null },
    { fieldName: 'Schedule', parentFieldName: 'ScheduleInfo', nodeType: 'GROUP', sourceType: null, occurrenceRule: 'ONE_OR_MORE', emptyHandling: 'REQUIRED', displayOrder: 1, valueType: null, defaultValue: null },
    { fieldName: 'ScheduleNo', parentFieldName: 'Schedule', nodeType: 'ATTRIBUTE', sourceType: 'INPUT', occurrenceRule: null, emptyHandling: 'REQUIRED', displayOrder: 1, valueType: 'STRING', defaultValue: null },
];

const scheduleSample = {
    Schedule: [
        { ScheduleNo: '1' },
        { ScheduleNo: '2' },
        { ScheduleNo: '3' },
        { ScheduleNo: '4' },
        { ScheduleNo: '5' },
    ],
};

const footballFields = [
    { fieldName: 'Football', parentFieldName: null, nodeType: 'GROUP', sourceType: null, occurrenceRule: 'ONE_OR_MORE', emptyHandling: 'REQUIRED', displayOrder: 1, valueType: null, defaultValue: null },
    { fieldName: 'GameSchedule', parentFieldName: 'Football', nodeType: 'GROUP', sourceType: null, occurrenceRule: null, emptyHandling: 'REQUIRED', displayOrder: 1, valueType: null, defaultValue: null },
    { fieldName: 'SendDate', parentFieldName: 'GameSchedule', nodeType: 'ELEMENT', sourceType: 'INPUT', occurrenceRule: null, emptyHandling: 'REQUIRED', displayOrder: 1, valueType: 'STRING', defaultValue: null },
    { fieldName: 'GameCategory', parentFieldName: 'GameSchedule', nodeType: 'GROUP', sourceType: null, occurrenceRule: 'ONE_OR_MORE', emptyHandling: 'REQUIRED', displayOrder: 2, valueType: null, defaultValue: null },
    { fieldName: 'SeasonID', parentFieldName: 'GameCategory', nodeType: 'ELEMENT', sourceType: 'MASTER_DATA', occurrenceRule: null, emptyHandling: 'REQUIRED', displayOrder: 1, valueType: 'STRING', defaultValue: null },
    { fieldName: 'SeasonName', parentFieldName: 'GameCategory', nodeType: 'ELEMENT', sourceType: 'MASTER_DATA', occurrenceRule: null, emptyHandling: 'REQUIRED', displayOrder: 2, valueType: 'STRING', defaultValue: null },
];

const footballSample = {
    GameSchedule: {
        SendDate: '20260101',
        GameCategory: [
            { SeasonID: '10', SeasonName: 'Spring' },
            { SeasonID: '11', SeasonName: 'Summer' },
        ],
    },
};

// Simulate OLD mergeSavedInputIntoFormData + OLD formDataToInputData (INPUT-only, array requires array)
function oldMerge(defaults, saved) {
    const merged = { ...defaults };
    for (const [key, value] of Object.entries(saved)) {
        if (Array.isArray(value)) {
            merged[key] = value;
        } else if (value !== null && typeof value === 'object') {
            const base = merged[key] && typeof merged[key] === 'object' && !Array.isArray(merged[key]) ? merged[key] : {};
            merged[key] = { ...base, ...value };
        } else {
            merged[key] = value;
        }
    }
    return merged;
}

function oldBuildDefault(fields) {
    const result = {};
    for (const f of fields.filter((x) => x.parentFieldName === 'ScheduleInfo' && x.fieldName === 'Schedule')) {
        result.Schedule = [{}];
    }
    for (const f of fields.filter((x) => x.parentFieldName === 'Football')) {
        if (f.fieldName === 'GameSchedule') {
            result.GameSchedule = {};
        }
    }
    return result;
}

function oldFormToInput(fields, form) {
    const out = {};
    const schedule = form.Schedule;
    if (Array.isArray(schedule)) {
        out.Schedule = schedule;
    } else {
        out.Schedule = [];
    }
    const gs = form.GameSchedule;
    if (gs && typeof gs === 'object' && !Array.isArray(gs)) {
        out.GameSchedule = gs;
    } else {
        out.GameSchedule = [];
    }
    return out;
}

console.log('=== Scenario A: Schedule x5 with OLD merge + defaults ===');
const oldDefaults = { Schedule: [{}] };
const oldMerged = oldMerge(oldDefaults, scheduleSample);
console.log('merged Schedule length:', Array.isArray(oldMerged.Schedule) ? oldMerged.Schedule.length : 'not array');
console.log('merged Schedule:', JSON.stringify(oldMerged.Schedule));

console.log('\n=== Scenario B: GameSchedule object vs default empty ===');
const oldFbDefaults = { GameSchedule: {} };
const oldFbMerged = oldMerge(oldFbDefaults, footballSample);
console.log('merged GameSchedule:', JSON.stringify(oldFbMerged.GameSchedule));

console.log('\n=== Scenario C: non-array repeatable write (OLD) ===');
const formWithObject = { GameSchedule: { SendDate: 'x' } };
console.log('output GameSchedule:', JSON.stringify(oldFormToInput(footballFields, formWithObject).GameSchedule));

console.log('\n=== Scenario D: MASTER_DATA excluded from form (OLD INPUT-only) ===');
const inputOnlyCount = footballFields.filter((f) => (f.nodeType === 'ELEMENT' || f.nodeType === 'ATTRIBUTE') && f.sourceType === 'INPUT').length;
const scalarCount = footballFields.filter((f) => (f.nodeType === 'ELEMENT' || f.nodeType === 'ATTRIBUTE') && (f.sourceType === 'INPUT' || f.sourceType === 'MASTER_DATA')).length;
console.log('INPUT-only field count:', inputOnlyCount, '(SeasonID/SeasonName excluded:', inputOnlyCount === 1, ')');
console.log('INPUT+MASTER_DATA field count:', scalarCount);
