/**
 * Phase 2: schedule-4.xml with saved-input override path.
 */
import { chromium } from 'playwright';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const XML_PATH = resolve(__dirname, '../../schedule-4.xml');
const BASE_URL = process.env.XMLGEN_BASE_URL ?? 'http://localhost:5173';
const TEMPLATE_CODE = `SCHED4_SAVE_${Date.now()}`;
const CHECKPOINT_PREFIX = '[XMLGEN-INVESTIGATION]';
const EXPECTED_COUNTS = [30, 2, 0];

function summarizeNested(data) {
    if (!data || typeof data !== 'object') {
        return { gameCategoryCount: 0, scheduleInfoCountsPerCategory: [], flattenedScheduleInfoAtGameSchedule: false };
    }
    let flattened = false;
    let gs = data.GameSchedule;
    if (Array.isArray(gs) && gs.length === 1) gs = gs[0];
    if (gs && typeof gs === 'object' && !Array.isArray(gs) && 'ScheduleInfo' in gs) flattened = true;
    const cats = Array.isArray(gs) ? gs : gs?.GameCategory;
    const list = Array.isArray(cats) ? cats : cats ? [cats] : [];
    return {
        gameCategoryCount: list.length,
        scheduleInfoCountsPerCategory: list.map((c) => (Array.isArray(c?.ScheduleInfo) ? c.ScheduleInfo.length : c?.ScheduleInfo ? 1 : 0)),
        flattenedScheduleInfoAtGameSchedule: flattened,
    };
}

async function importAndSave(page, network) {
    await page.goto(`${BASE_URL}/login`);
    await page.fill('#username', 'admin');
    await page.fill('#password', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/templates**', { timeout: 30000 });

    const importResp = page.waitForResponse(
        (r) => r.url().includes('/api/v1/templates/import') && r.request().method() === 'POST',
        { timeout: 120000 },
    );
    await page.getByRole('button', { name: /Import XML/i }).click();
    await page.locator('input[type="file"]').setInputFiles(XML_PATH);
    await importResp;
    network.importDraft = await (await importResp).json();
    await page.waitForURL('**/templates/import**');
    const inputs = page.locator('form input:not([type="file"]):not([type="checkbox"])');
    await inputs.nth(0).fill(TEMPLATE_CODE);
    await inputs.nth(1).fill(`Save test ${TEMPLATE_CODE}`);
    await page.getByRole('button', { name: /Save schema/i }).click();
    await page.waitForURL('**/schema**', { timeout: 120000 });
}

async function selectTemplate(page) {
    await page.goto(`${BASE_URL}/xml-generation`);
    await page.waitForSelector('#template-select', { timeout: 30000 });
    await page.selectOption('#template-select', { label: `${TEMPLATE_CODE} — Save test ${TEMPLATE_CODE}` });
    await page.waitForTimeout(5000);
}

async function main() {
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
        if (response.url().includes('/api/v1/saved-inputs/template/') && response.request().method() === 'GET') {
            network.savedInput = await response.json();
        }
    });

    await importAndSave(page, network);
    await selectTemplate(page);

    const fresh = await page.evaluate(() => window.__XMLGEN_INVESTIGATION__ ?? null);
    const freshSummary = summarizeNested(fresh?.['1_sampleInputJson_fromApi']?.raw ?? fresh?.['1_sampleInputJson_fromApi']);
    const freshResolved = fresh?.['3_resolvedInitialFormData']?.summary;

    console.log('FRESH PATH sampleInputJson:', JSON.stringify(freshSummary));
    console.log('FRESH PATH resolved:', JSON.stringify(freshResolved));
    console.log('FRESH savedInput:', JSON.stringify(fresh?.['2_savedInput_fromApi']));

    // Export minimal broken saved input
    await page.getByRole('button', { name: /^JSON$/i }).click();
    await page.waitForTimeout(500);
    await page.locator('textarea').first().fill('{ "GameSchedule": { "GameCategory": [] } }');
    await page.getByRole('button', { name: /^Export$/i }).click();
    await page.waitForTimeout(4000);

    await page.evaluate(() => { window.__XMLGEN_INVESTIGATION__ = {}; });
    await page.goto(`${BASE_URL}/templates`);
    await page.waitForTimeout(1000);
    await selectTemplate(page);

    const after = await page.evaluate(() => window.__XMLGEN_INVESTIGATION__ ?? null);
    const savedInput = after?.['2_savedInput_fromApi'];
    const afterResolved = after?.['3_resolvedInitialFormData']?.summary;
    const afterSample = summarizeNested(after?.['1_sampleInputJson_fromApi']?.raw ?? after?.['1_sampleInputJson_fromApi']);
    const paths = Object.entries(after ?? {})
        .filter(([k]) => k.startsWith('6_repeatable_'))
        .map(([k, v]) => ({ key: k, parentPath: v?.parentPath, itemCount: v?.itemCount }));

    console.log('\n=== SAVED INPUT OVERRIDE PATH ===');
    console.log('savedInput from API:', JSON.stringify(savedInput));
    console.log('sampleInputJson (still from template):', JSON.stringify(afterSample));
    console.log('resolveInitialFormData summary:', JSON.stringify(afterResolved));
    console.log('repeatable component paths:', JSON.stringify(paths, null, 2));

    const countsMatch = (s) =>
        s?.gameCategoryCount === 3
        && JSON.stringify(s?.scheduleInfoCountsPerCategory) === JSON.stringify(EXPECTED_COUNTS);

    const rows = [
        {
            checkpoint: '2. sampleInputJson (after reload)',
            expected: '[30,2,0] nested per GameCategory',
            actual: JSON.stringify(afterSample),
            status: countsMatch(afterSample) ? 'PASS' : 'FAIL',
        },
        {
            checkpoint: '2b. savedInput from API',
            expected: 'Should not wipe nested ScheduleInfo if preserving hierarchy',
            actual: JSON.stringify(savedInput?.inputData ?? savedInput),
            status: savedInput?.inputData?.GameSchedule?.GameCategory?.length === 0 ? 'FAIL (empty GameCategory[])' : 'PASS',
        },
        {
            checkpoint: '3. resolveInitialFormData()',
            expected: '[30,2,0]',
            actual: JSON.stringify(afterResolved),
            status: countsMatch(afterResolved) ? 'PASS' : 'FAIL',
        },
    ];

    console.log('\n| Checkpoint | Expected | Actual | Status |');
    for (const r of rows) {
        console.log(`| ${r.checkpoint} | ${r.expected} | ${r.actual.slice(0, 100)} | ${r.status} |`);
    }

    await browser.close();
}

main().catch((e) => { console.error(e); process.exit(1); });
