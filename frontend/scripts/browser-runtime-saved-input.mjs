/**
 * Phase 2: Saved Input override path — export broken JSON then reload.
 */
import { chromium } from 'playwright';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, '../..');
const XML_PATH = resolve(ROOT, 'test_data/investigation_football.xml');
const BASE_URL = process.env.XMLGEN_BASE_URL ?? 'http://localhost:5173';
const TEMPLATE_CODE = `INV_SAVE_${Date.now()}`;
const CHECKPOINT_PREFIX = '[XMLGEN-INVESTIGATION]';

async function loginAndImport(page, networkCapture) {
    await page.goto(`${BASE_URL}/login`);
    await page.fill('#username', 'admin');
    await page.fill('#password', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/templates**', { timeout: 30000 });

    const importResponsePromise = page.waitForResponse(
        (response) =>
            response.url().includes('/api/v1/templates/import') && response.request().method() === 'POST',
        { timeout: 30000 },
    );
    await page.getByRole('button', { name: /Import XML/i }).click();
    await page.locator('input[type="file"]').setInputFiles(XML_PATH);
    const importResponse = await importResponsePromise;
    networkCapture.importDraft = await importResponse.json();
    await page.waitForURL('**/templates/import**', { timeout: 30000 });
    await page.getByText('Template metadata').waitFor({ timeout: 30000 });

    const metadataInputs = page.locator('form input:not([type="file"]):not([type="checkbox"])');
    await metadataInputs.nth(0).fill(TEMPLATE_CODE);
    await metadataInputs.nth(1).fill(`Investigation ${TEMPLATE_CODE}`);
    await page.getByRole('button', { name: /Save schema/i }).click();
    await page.waitForURL('**/schema**', { timeout: 60000 });
}

async function openXmlGeneration(page, templateCode) {
    await page.goto(`${BASE_URL}/xml-generation`);
    await page.waitForSelector('#template-select', { timeout: 30000 });
    await page.selectOption('#template-select', { label: `${templateCode} — Investigation ${templateCode}` });
    await page.waitForTimeout(4000);
}

async function collectCapture(page, captured) {
    return {
        consoleCapture: { ...captured },
        windowCapture: await page.evaluate(() => window.__XMLGEN_INVESTIGATION__ ?? null),
        domScheduleLabels: await page.locator('text=/Schedule #\\d+/').count(),
    };
}

async function main() {
    const captured = {};
    const networkCapture = {};
    const browser = await chromium.launch({ headless: true });
    const page = await browser.newPage();

    page.on('console', (message) => {
        const text = message.text();
        if (!text.includes(CHECKPOINT_PREFIX)) {
            return;
        }
        const label = text.replace(`${CHECKPOINT_PREFIX} `, '').split(' ')[0];
        captured[label] = text;
    });

    page.on('response', async (response) => {
        const url = response.url();
        try {
            if (url.includes('/api/v1/saved-inputs/template/') && response.request().method() === 'GET') {
                networkCapture.savedInput = await response.json();
            }
            if (url.includes('/api/v1/xml-generation/export') && response.request().method() === 'POST') {
                networkCapture.exportRequestBody = response.request().postDataJSON();
            }
        } catch {
            // ignore
        }
    });

    await loginAndImport(page, networkCapture);
    await openXmlGeneration(page, TEMPLATE_CODE);

    console.log('Phase A: Fresh import (no saved input)');
    const freshCapture = await collectCapture(page, captured);
    console.log(JSON.stringify(freshCapture, null, 2));

    Object.keys(captured).forEach((key) => delete captured[key]);
    await page.evaluate(() => {
        window.__XMLGEN_INVESTIGATION__ = {};
    });

    console.log('\nPhase B: Export broken JSON to create saved input');
    await page.getByRole('button', { name: /^JSON$/i }).click();
    await page.waitForTimeout(500);
    const jsonEditor = page.locator('textarea').first();
    await jsonEditor.fill('{ "GameSchedule": [] }');
    await page.waitForTimeout(500);
    await page.getByRole('button', { name: /^Export$/i }).click();
    await page.waitForTimeout(4000);

    Object.keys(captured).forEach((key) => delete captured[key]);
    await page.evaluate(() => {
        window.__XMLGEN_INVESTIGATION__ = {};
    });

    console.log('\nPhase C: Reload XML Generation (saved input should override)');
    await page.goto(`${BASE_URL}/templates`);
    await page.waitForTimeout(1000);
    await openXmlGeneration(page, TEMPLATE_CODE);

    const savedInputCapture = await collectCapture(page, captured);
    const savedInputResponse = networkCapture.savedInput;

    console.log(JSON.stringify({
        exportRequestBody: networkCapture.exportRequestBody,
        savedInputResponse,
        savedInputCapture,
    }, null, 2));

    const getScheduleCount = (capture) => {
        const win = capture.windowCapture;
        return {
            apiSample: win?.['1_sampleInputJson_fromApi']?.ScheduleInfo?.Schedule?.length ?? null,
            savedInput: win?.['2_savedInput_fromApi']?.inputData ?? null,
            resolved: win?.['3_resolvedInitialFormData']?.ScheduleInfo?.[0]?.Schedule?.length
                ?? (Array.isArray(win?.['3_resolvedInitialFormData']?.ScheduleInfo)
                    ? win['3_resolvedInitialFormData'].ScheduleInfo[0]?.Schedule?.length
                    : null),
            repeatable: win?.['6_repeatableSchedule_componentData']?.itemCount ?? null,
            preview: win?.['7_previewRequestPayload']?.body?.inputData?.ScheduleInfo?.[0]?.Schedule?.length ?? null,
            dom: capture.domScheduleLabels,
        };
    };

    console.log('\n========== COMPARISON ==========');
    console.log(JSON.stringify({
        fresh: getScheduleCount(freshCapture),
        afterSavedInputOverride: getScheduleCount(savedInputCapture),
    }, null, 2));

    await browser.close();
}

main().catch((error) => {
    console.error(error);
    process.exit(1);
});
