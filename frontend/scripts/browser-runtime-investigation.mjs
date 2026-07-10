/**
 * Browser runtime investigation — drives the real app UI and captures console checkpoints.
 * Run: node scripts/browser-runtime-investigation.mjs
 */
import { chromium } from 'playwright';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, '../..');
const XML_PATH = resolve(ROOT, 'test_data/investigation_football.xml');
const BASE_URL = process.env.XMLGEN_BASE_URL ?? 'http://localhost:5173';
const USERNAME = 'admin';
const PASSWORD = 'admin123';
const TEMPLATE_CODE = `INV_FOOT_${Date.now()}`;

const CHECKPOINT_PREFIX = '[XMLGEN-INVESTIGATION]';

async function waitForBackend() {
    for (let attempt = 0; attempt < 30; attempt++) {
        try {
            const response = await fetch('http://localhost:8080/actuator/health');
            if (response.ok) {
                return;
            }
        } catch {
            // retry
        }
        await new Promise((resolveDelay) => setTimeout(resolveDelay, 2000));
    }
    throw new Error('Backend not reachable at http://localhost:8080');
}

async function main() {
    await waitForBackend();

    const captured = {};
    const networkCapture = {};

    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    page.on('console', (message) => {
        const text = message.text();
        if (!text.includes(CHECKPOINT_PREFIX)) {
            return;
        }
        const label = text.replace(`${CHECKPOINT_PREFIX} `, '').split(' ')[0];
        try {
            const jsonStart = text.indexOf('{');
            const arrayStart = text.indexOf('[');
            const start =
                jsonStart === -1
                    ? arrayStart
                    : arrayStart === -1
                      ? jsonStart
                      : Math.min(jsonStart, arrayStart);
            if (start !== -1) {
                captured[label] = JSON.parse(text.slice(start));
            }
        } catch {
            captured[label] = text;
        }
    });

    page.on('response', async (response) => {
        const url = response.url();
        try {
            if (url.includes('/api/v1/templates/import') && response.request().method() === 'POST') {
                networkCapture.importDraft = await response.json();
            }
            if (/\/api\/v1\/templates\/\d+$/.test(url) && response.request().method() === 'GET') {
                networkCapture.templateDetail = await response.json();
            }
            if (url.includes('/api/v1/saved-inputs/template/') && response.request().method() === 'GET') {
                networkCapture.savedInput = await response.json();
            }
            if (url.includes('/api/v1/xml-generation/preview') && response.request().method() === 'POST') {
                networkCapture.previewRequestBody = response.request().postDataJSON();
                networkCapture.previewResponse = await response.json();
            }
        } catch {
            // ignore parse errors
        }
    });

    console.log('Step 1: Login via UI');
    await page.goto(`${BASE_URL}/login`);
    await page.fill('#username', USERNAME);
    await page.fill('#password', PASSWORD);
    await page.click('button[type="submit"]');
    await page.waitForURL('**/templates**', { timeout: 30000 });

    console.log('Step 2: Import XML via UI file picker');
    const importResponsePromise = page.waitForResponse(
        (response) =>
            response.url().includes('/api/v1/templates/import') && response.request().method() === 'POST',
        { timeout: 30000 },
    );
    await page.getByRole('button', { name: /Import XML/i }).click();
    await page.locator('input[type="file"]').setInputFiles(XML_PATH);
    const importResponse = await importResponsePromise;
    const importJson = await importResponse.json();
    networkCapture.importDraft = importJson;
    await page.waitForURL('**/templates/import**', { timeout: 30000 });
    await page.getByText('Template metadata').waitFor({ timeout: 30000 });

    const importDraft = networkCapture.importDraft?.data;
    console.log(
        'Import draft sampleInputJson Schedule count:',
        importDraft?.sampleInputJson?.ScheduleInfo?.Schedule?.length ?? 'n/a',
    );

    console.log('Step 3: Save imported template');
    const metadataInputs = page.locator('form input:not([type="file"]):not([type="checkbox"])');
    await metadataInputs.nth(0).waitFor({ timeout: 30000 });
    await metadataInputs.nth(0).fill(TEMPLATE_CODE);
    await metadataInputs.nth(1).fill(`Investigation ${TEMPLATE_CODE}`);
    await page.getByRole('button', { name: /Save schema/i }).click();
    await page.waitForURL('**/templates/**/schema**', { timeout: 60000 });

    console.log('Step 4: Open XML Generation and select template');
    await page.goto(`${BASE_URL}/xml-generation`);
    await page.waitForSelector('#template-select', { timeout: 30000 });
    await page.selectOption('#template-select', { label: `${TEMPLATE_CODE} — Investigation ${TEMPLATE_CODE}` });
    await page.waitForTimeout(4000);

    console.log('Step 5: Click Preview');
    await page.getByRole('button', { name: /^Preview$/i }).click();
    await page.waitForTimeout(4000);

    const windowCapture = await page.evaluate(() => window.__XMLGEN_INVESTIGATION__ ?? null);
    const scheduleRows = await page.locator('text=/Schedule #\\d+/').count();

    const report = {
        templateCode: TEMPLATE_CODE,
        networkCapture: {
            importSampleInputJson: importDraft?.sampleInputJson ?? null,
            templateDetailSampleInputJson: networkCapture.templateDetail?.data?.sampleInputJson ?? null,
            savedInputResponse: networkCapture.savedInput ?? null,
            previewRequestBody: networkCapture.previewRequestBody ?? null,
            previewResponse: networkCapture.previewResponse ?? null,
        },
        consoleCapture: captured,
        windowCapture,
        domScheduleOccurrenceLabels: scheduleRows,
    };

    console.log('\n========== BROWSER RUNTIME CAPTURE ==========\n');
    console.log(JSON.stringify(report, null, 2));

    const scheduleCounts = {
        importApi: importDraft?.sampleInputJson?.ScheduleInfo?.Schedule?.length ?? null,
        checkpoint1_apiSampleInputJson:
            captured['1_sampleInputJson_fromApi']?.ScheduleInfo?.Schedule?.length ?? null,
        checkpoint2_savedInputExists: captured['2_savedInput_fromApi'] != null,
        checkpoint2_savedInputScheduleLength:
            captured['2_savedInput_fromApi']?.inputData?.ScheduleInfo?.Schedule?.length ?? null,
        checkpoint3_resolvedInitialFormData:
            captured['3_resolvedInitialFormData']?.ScheduleInfo?.Schedule?.length ?? null,
        checkpoint4_resolvedCount:
            captured['4_reactState_beforeFirstRender']?.scheduleItemCountInResolvedFormData ?? null,
        checkpoint4_effectiveCount:
            captured['4_reactState_beforeFirstRender']?.scheduleItemCountInEffectiveFormData ?? null,
        checkpoint5_DynamicInputForm:
            captured['5_DynamicInputForm_props']?.scheduleItemCount ?? null,
        checkpoint6_repeatableSchedule:
            captured['6_repeatableSchedule_componentData']?.itemCount ?? null,
        checkpoint7_previewPayload:
            captured['7_previewRequestPayload']?.scheduleArrayLengthInInputData ?? null,
        domScheduleLabels: scheduleRows,
    };

    console.log('\n========== SCHEDULE COUNT SUMMARY ==========\n');
    console.log(JSON.stringify(scheduleCounts, null, 2));

    let firstDrop = null;
    const chain = [
        ['importApi', scheduleCounts.importApi],
        ['checkpoint1_sampleInputJson_fromApi', scheduleCounts.checkpoint1_apiSampleInputJson],
        ['checkpoint3_resolvedInitialFormData', scheduleCounts.checkpoint3_resolvedInitialFormData],
        ['checkpoint4_reactState_resolved', scheduleCounts.checkpoint4_resolvedCount],
        ['checkpoint5_DynamicInputForm', scheduleCounts.checkpoint5_DynamicInputForm],
        ['checkpoint6_repeatableSchedule', scheduleCounts.checkpoint6_repeatableSchedule],
        ['checkpoint7_previewPayload', scheduleCounts.checkpoint7_previewPayload],
        ['domScheduleLabels', scheduleCounts.domScheduleLabels],
    ];
    for (let index = 1; index < chain.length; index++) {
        const [prevLabel, prevCount] = chain[index - 1];
        const [label, count] = chain[index];
        if (prevCount === 5 && count === 1) {
            firstDrop = { from: prevLabel, to: label, prevCount, count };
            break;
        }
        if (prevCount === 5 && count !== 5 && count !== null) {
            firstDrop = { from: prevLabel, to: label, prevCount, count };
            break;
        }
    }

    console.log('\n========== FIRST 5→1 DROP ==========\n');
    console.log(JSON.stringify(firstDrop, null, 2));

    await browser.close();
}

main().catch((error) => {
    console.error(error);
    process.exit(1);
});
