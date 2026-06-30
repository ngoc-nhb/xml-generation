export function parseJsonObject(text: string): { ok: true; value: Record<string, unknown> } | { ok: false; message: string } {
    const trimmed = text.trim();
    if (!trimmed) {
        return { ok: true, value: {} };
    }
    try {
        const parsed: unknown = JSON.parse(trimmed);
        if (parsed === null || typeof parsed !== 'object' || Array.isArray(parsed)) {
            return { ok: false, message: 'Input must be a JSON object.' };
        }
        return { ok: true, value: parsed as Record<string, unknown> };
    } catch {
        return { ok: false, message: 'Invalid JSON syntax.' };
    }
}

export function formatJson(text: string): { ok: true; formatted: string } | { ok: false; message: string } {
    const parsed = parseJsonObject(text);
    if (!parsed.ok) {
        return parsed;
    }
    return { ok: true, formatted: JSON.stringify(parsed.value, null, 2) };
}

export const INPUT_JSON_PLACEHOLDER = `{
  "GameDate": "2026-06-18",
  "Weather": "Sunny"
}`;

export const EMPTY_JSON = '{}';
