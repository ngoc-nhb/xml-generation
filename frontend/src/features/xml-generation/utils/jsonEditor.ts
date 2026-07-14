export interface JsonParseSuccess {
    ok: true;
    value: Record<string, unknown>;
}

export interface JsonParseFailure {
    ok: false;
    message: string;
    line?: number;
    column?: number;
    position?: number;
}

export type JsonParseResult = JsonParseSuccess | JsonParseFailure;

function offsetToLineColumn(text: string, position: number): { line: number; column: number } {
    const safePosition = Math.max(0, Math.min(position, text.length));
    let line = 1;
    let column = 1;
    for (let index = 0; index < safePosition; index += 1) {
        if (text[index] === '\n') {
            line += 1;
            column = 1;
        } else {
            column += 1;
        }
    }
    return { line, column };
}

function extractJsonErrorPosition(message: string): number | null {
    const atPosition = message.match(/at position\s+(\d+)/i);
    if (atPosition) {
        return Number.parseInt(atPosition[1]!, 10);
    }
    const atPos = message.match(/at pos\s+(\d+)/i);
    if (atPos) {
        return Number.parseInt(atPos[1]!, 10);
    }
    return null;
}

function readableJsonErrorMessage(raw: string): string {
    return raw
        .replace(/^JSON\.parse:\s*/i, '')
        .replace(/\s+in JSON at position \d+/i, '')
        .trim() || 'Invalid JSON syntax.';
}

export function parseJsonObject(text: string): JsonParseResult {
    if (!text.trim()) {
        return { ok: true, value: {} };
    }
    try {
        const parsed: unknown = JSON.parse(text);
        if (parsed === null || typeof parsed !== 'object' || Array.isArray(parsed)) {
            return { ok: false, message: 'Input must be a JSON object.' };
        }
        return { ok: true, value: parsed as Record<string, unknown> };
    } catch (error) {
        const rawMessage = error instanceof Error ? error.message : 'Invalid JSON syntax.';
        const position = extractJsonErrorPosition(rawMessage);
        if (position !== null) {
            const { line, column } = offsetToLineColumn(text, position);
            return {
                ok: false,
                message: readableJsonErrorMessage(rawMessage),
                line,
                column,
                position,
            };
        }
        return { ok: false, message: readableJsonErrorMessage(rawMessage) };
    }
}

export function formatJson(text: string): { ok: true; formatted: string } | JsonParseFailure {
    const parsed = parseJsonObject(text);
    if (!parsed.ok) {
        return parsed;
    }
    return { ok: true, formatted: JSON.stringify(parsed.value, null, 2) };
}

export function formatJsonValidationMessage(result: JsonParseFailure): string {
    if (result.line != null && result.column != null) {
        return `Invalid JSON\nLine ${result.line}\nColumn ${result.column}\n${result.message}`;
    }
    return result.message;
}

export function selectTextareaOffset(textarea: HTMLTextAreaElement, position: number): void {
    const safe = Math.max(0, Math.min(position, textarea.value.length));
    textarea.focus();
    textarea.setSelectionRange(safe, Math.min(safe + 1, textarea.value.length));
    const before = textarea.value.slice(0, safe);
    const lineHeight = Number.parseFloat(getComputedStyle(textarea).lineHeight) || 18;
    const line = before.split('\n').length;
    textarea.scrollTop = Math.max(0, (line - 3) * lineHeight);
}

export const INPUT_JSON_PLACEHOLDER = `{
  "GameDate": "2026-06-18",
  "Weather": "Sunny"
}`;

export const EMPTY_JSON = '{}';
