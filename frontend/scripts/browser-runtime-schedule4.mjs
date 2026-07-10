/**
 * Browser runtime investigation for schedule-4.xml (actual production XML).
 * Run: node scripts/browser-runtime-schedule4.mjs
 */
import { chromium } from 'playwright';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const XML_PATH = resolve(__dirname, '../../schedule-4.xml');
const BASE_URL = process.env.XMLGEN_BASE_URL ?? 'http://localhost:5173';
const TEMPLATE_CODE = `SCHED4_${Date.now()}`;
const CHECKPOINT_PREFIX = '[XMLGEN-INVESTIGATION]';

const EXPECTED = {
    gameCategoryCount: 3,
    scheduleInfoCounts: [30, 2, 0],
    schema: {
        gameCategoryParent: 'GameSchedule',
        scheduleInfoParent: 'GameCategory',
    },
};

function summarizeSchema(fields) {
    const gameCategory = fields.find((f) => f.fieldName === 'GameCategory');
    const scheduleInfo = fields.find((f) => f.fieldName === 'ScheduleInfo');
    return {
        gameCategoryParent: gameCategory?.parentFieldName ?? null,
        scheduleInfoParent: scheduleInfo?.parentFieldName ?? null,
        preservesNestedHierarchy:
            gameCategory?.parentFieldName === 'GameSchedule' && scheduleInfo?.parentFieldName === 'GameCategory',
        relevantFields: fields
            .filter((f) => f.fieldName === 'GameCategory' || f.fieldName === 'ScheduleInfo')
            .map((f) => ({
                fieldName: f.fieldName,
                parentFieldName: f.parentFieldName ?? null,
                occurrenceRule: f.occurrenceRule ?? null,
            })),
    };
}

function countScheduleInfoItems(value) {
    if (Array.isArray(value)) return value.length;
    if (value != null && typeof value === 'object') return 1;
    return 0;
}

function summarizeNested(data) {
    if (data == null || typeof data !== 'object') {
        return {
            gameCategoryCount: 0,
            scheduleInfoCountsPerCategory: [],
            scheduleInfoParentPaths: [],
            flattenedScheduleInfoAtGameSchedule: false,
        };
    }
    const paths = [];
    let flattened = false;
    let gameSchedule = data.GameSchedule;
    if (Array.isArray(gameSchedule) && gameSchedule.length === 1 && typeof gameSchedule[0] === 'object') {
        gameSchedule = gameSchedule[0];
    }
    if (gameSchedule != null && typeof gameSchedule === 'object' && !Array.isArray(gameSchedule)) {
        if ('ScheduleInfo' in gameSchedule) {
            flattened = true;
            paths.push('GameSchedule.ScheduleInfo');
        }
    }
    const categories = Array.isArray(gameSchedule)
        ? gameSchedule
        : gameSchedule?.GameCategory;
    const list = Array.isArray(categories) ? categories : categories != null ? [categories] : [];
    const counts = list.map((cat, i) => {
        if (cat?.ScheduleInfo !== undefined) {
            paths.push(`GameSchedule.GameCategory[${i}].ScheduleInfo`);
        }
        return countScheduleInfoItems(cat?.ScheduleInfo);
    });
    return {
        gameCategoryCount: list.length,
        scheduleInfoCountsPerCategory: counts,
        scheduleInfoParentPaths: paths,
        flattenedScheduleInfoAtGameSchedule: flattened,
    };
}

function matchesExpected(summary) {
    if (summary.gameCategoryCount !== EXPECTED.gameCategoryCount) return false;
    if (summary.flattenedScheduleInfoAtGameSchedule) return false;
    if (summary.scheduleInfoCountsPerCategory.length !== EXPECTED.scheduleInfoCounts.length) return false;
    return summary.scheduleInfoCountsPerCategory.every(
        (count, i) => count === EXPECTED.scheduleInfoCounts[i],
    );
}

async function waitForBackend() {
    for (let i = 0; i < 30; i++) {
        try {
            const r = await fetch('http://localhost:8080/actuator/health');
            if (r.ok) return;
        } catch {}
        await new Promise((r) => setTimeout(r, 2000));
    }
    throw new Error('Backend not reachable');
}

async function main() {
    await waitForBackend();

    const captured = {};
    const network = {};
    const browser = await chromium.launch({ headless: true });
    const page = await browser.newPage();

    page.on('console', (msg) => {
        const text = msg.text();
        if (!text.includes(CHECKPOINT_PREFIX)) return;
        const label = text.replace(`${CHECKPOINT_PREFIX} `, '').split(' ')[0];
        try {
            const start = Math.min(
                text.indexOf('{') === -1 ? Infinity : text.indexOf('{'),
                text.indexOf('[') === -1 ? Infinity : text.indexOf('['),
            );
            if (start !== Infinity) captured[label] = JSON.parse(text.slice(start));
        } catch {
            captured[label] = text;
        }
    });

    page.on('response', async (response) => {
        const url = response.url();
        try {
            if (url.includes('/api/v1/templates/import') && response.request().method() === 'POST') {
                network.importDraft = await response.json();
            }
            if (/\/api\/v1\/templates\/\d+$/.test(url) && response.request().method() === 'GET') {
                network.templateDetail = await response.json();
            }
            if (url.includes('/api/v1/saved-inputs/template/') && response.request().method() === 'GET') {
                network.savedInput = await response.json();
            }
            if (url.includes('/api/v1/xml-generation/preview') && response.request().method() === 'POST') {
                network.previewRequestBody = response.request().postDataJSON();
                network.previewResponse = await response.json();
            }
        } catch {}
    });

    // Login
    await page.goto(`${BASE_URL}/login`);
    await page.fill('#username', 'admin');
    await page.fill('#password', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/templates**', { timeout: 30000 });

    // Import schedule-4.xml
    const importResp = page.waitForResponse(
        (r) => r.url().includes('/api/v1/templates/import') && r.request().method() === 'POST',
        { timeout: 120000 },
    );
    await page.getByRole('button', { name: /Import XML/i }).click();
    await page.locator('input[type="file"]').setInputFiles(XML_PATH);
    await importResp;
    await page.waitForURL('**/templates/import**', { timeout: 30000 });
    await page.getByText('Template metadata').waitFor({ timeout: 30000 });

    const importDraft = network.importDraft?.data;
    const importSchemaSummary = summarizeSchema(importDraft?.fields ?? []);
    const importSampleSummary = summarizeNested(importDraft?.sampleInputJson);

    // Save template
    const inputs = page.locator('form input:not([type="file"]):not([type="checkbox"])');
    await inputs.nth(0).fill(TEMPLATE_CODE);
    await inputs.nth(1).fill(`Schedule4 ${TEMPLATE_CODE}`);
    await page.getByRole('button', { name: /Save schema/i }).click();
    await page.waitForURL('**/schema**', { timeout: 120000 });

    // XML Generation
    await page.goto(`${BASE_URL}/xml-generation`);
    await page.waitForSelector('#template-select', { timeout: 30000 });
    await page.selectOption('#template-select', { label: `${TEMPLATE_CODE} — Schedule4 ${TEMPLATE_CODE}` });
    await page.waitForTimeout(5000);

    await page.getByRole('button', { name: /^Preview$/i }).click();
    await page.waitForTimeout(5000);

    const windowCapture = await page.evaluate(() => window.__XMLGEN_INVESTIGATION__ ?? null);

    const schemaFromRuntime = windowCapture?.['0_templateSchema_hierarchy'] ?? summarizeSchema(
        network.templateDetail?.data?.schema?.fields ?? importDraft?.fields ?? [],
    );
    const sampleFromRuntime = windowCapture?.['1_sampleInputJson_fromApi']?.summary
        ?? summarizeNested(windowCapture?.['1_sampleInputJson_fromApi']?.raw
            ?? network.templateDetail?.data?.sampleInputJson
            ?? importDraft?.sampleInputJson);
    const resolvedFromRuntime = windowCapture?.['3_resolvedInitialFormData']?.summary
        ?? summarizeNested(windowCapture?.['3_resolvedInitialFormData']);
    const reactFromRuntime = windowCapture?.['4_reactState_beforeFirstRender']?.nestedSummaryFromEffectiveFormData
        ?? summarizeNested(windowCapture?.['4_reactState_beforeFirstRender']);
    const formFromRuntime = windowCapture?.['5_DynamicInputForm_props']?.nestedSummary
        ?? summarizeNested(windowCapture?.['5_DynamicInputForm_props']?.value);
    const previewFromRuntime = windowCapture?.['7_previewRequestPayload']?.nestedSummary
        ?? summarizeNested(network.previewRequestBody?.inputData);

    const scheduleInfoPaths = Object.entries(windowCapture ?? {})
        .filter(([k]) => k.startsWith('6_repeatable_'))
        .map(([k, v]) => ({ key: k, parentPath: v?.parentPath ?? v?.groupKey, itemCount: v?.itemCount }));

    const rows = [];
    const add = (checkpoint, expected, actual, status) => rows.push({ checkpoint, expected, actual, status });

    // Checkpoint 1: Schema
    const schemaOk =
        schemaFromRuntime.gameCategoryParent === EXPECTED.schema.gameCategoryParent
        && schemaFromRuntime.scheduleInfoParent === EXPECTED.schema.scheduleInfoParent;
    add(
        '1. Template Schema',
        `GameCategory parent=GameSchedule, ScheduleInfo parent=GameCategory`,
        JSON.stringify(schemaFromRuntime),
        schemaOk ? 'PASS' : 'FAIL',
    );

    // Checkpoint 2: sampleInputJson (use import if runtime not ready)
    const sampleSummary = importSampleSummary.gameCategoryCount > 0 ? importSampleSummary : sampleFromRuntime;
    const sampleOk = matchesExpected(sampleSummary);
    add(
        '2. sampleInputJson',
        `GameCategory=3, ScheduleInfo per category=[30,2,0], nested under each GameCategory`,
        JSON.stringify(sampleSummary),
        sampleOk ? 'PASS' : 'FAIL',
    );

    let firstFail = rows.find((r) => r.status === 'FAIL');

    if (!firstFail) {
        const resolvedOk = matchesExpected(resolvedFromRuntime);
        add(
            '3. resolveInitialFormData()',
            `GameCategory=3, ScheduleInfo per category=[30,2,0]`,
            JSON.stringify(resolvedFromRuntime),
            resolvedOk ? 'PASS' : 'FAIL',
        );
        firstFail = rows.find((r) => r.status === 'FAIL');
    } else {
        add('3. resolveInitialFormData()', '(skipped — prior failure)', '-', 'SKIP');
    }

    if (!firstFail) {
        const reactOk = matchesExpected(reactFromRuntime);
        add(
            '4. React State',
            `GameCategory=3, ScheduleInfo per category=[30,2,0]`,
            JSON.stringify(reactFromRuntime),
            reactOk ? 'PASS' : 'FAIL',
        );
        firstFail = rows.find((r) => r.status === 'FAIL');
    } else {
        add('4. React State', '(skipped)', '-', 'SKIP');
    }

    if (!firstFail) {
        const pathsOk = scheduleInfoPaths.every((p) => p.parentPath?.includes('GameCategory'));
        const formOk = matchesExpected(formFromRuntime) && pathsOk;
        add(
            '5. DynamicInputForm',
            `Parent paths like GameSchedule.GameCategory[N].ScheduleInfo; counts [30,2,0]`,
            JSON.stringify({ nestedSummary: formFromRuntime, repeatablePaths: scheduleInfoPaths }),
            formOk ? 'PASS' : 'FAIL',
        );
        firstFail = rows.find((r) => r.status === 'FAIL');
    } else {
        add('5. DynamicInputForm', '(skipped)', '-', 'SKIP');
    }

    if (!firstFail) {
        const previewOk = matchesExpected(previewFromRuntime);
        add(
            '6. Preview Request',
            `GameCategory=3, ScheduleInfo per category=[30,2,0]`,
            JSON.stringify(previewFromRuntime),
            previewOk ? 'PASS' : 'FAIL',
        );
        firstFail = rows.find((r) => r.status === 'FAIL');
    } else {
        add('6. Preview Request', '(skipped)', '-', 'SKIP');
    }

    console.log('\n========== SCHEDULE-4.XML INVESTIGATION ==========\n');
    console.log('Template code:', TEMPLATE_CODE);
    console.log('\n--- Import-time schema (before save) ---');
    console.log(JSON.stringify(importSchemaSummary, null, 2));
    console.log('\n--- Import-time sampleInputJson summary ---');
    console.log(JSON.stringify(importSampleSummary, null, 2));
    console.log('\n--- Checkpoint Table ---\n');
    console.log('| Checkpoint | Expected | Actual | Status |');
    console.log('|------------|----------|--------|--------|');
    for (const row of rows) {
        const exp = row.expected.replace(/\|/g, '\\|').slice(0, 80);
        const act = row.actual.replace(/\|/g, '\\|').slice(0, 120);
        console.log(`| ${row.checkpoint} | ${exp} | ${act} | ${row.status} |`);
    }
    console.log('\n--- First mismatch (root cause) ---');
    console.log(firstFail ? JSON.stringify(firstFail, null, 2) : 'None — all checkpoints passed');
    console.log('\n--- Full window capture keys ---');
    console.log(Object.keys(windowCapture ?? {}).join(', '));

    await browser.close();
}

main().catch((e) => {
    console.error(e);
    process.exit(1);
});
