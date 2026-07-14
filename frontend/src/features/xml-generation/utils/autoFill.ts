/**
 * Spreadsheet-style auto-fill series for Bulk Edit cells.
 */

export interface ParsedNumericToken {
    prefix: string;
    number: bigint;
    width: number;
    suffix: string;
}

const TRAILING_NUMBER = /^(.*?)(-?\d+)(.*)$/;

export function parseTrailingNumber(value: string): ParsedNumericToken | null {
    const match = value.match(TRAILING_NUMBER);
    if (!match) {
        return null;
    }
    const [, prefix, digits, suffix] = match;
    if (!digits) {
        return null;
    }
    try {
        return {
            prefix: prefix ?? '',
            number: BigInt(digits),
            width: digits.startsWith('-') ? digits.length - 1 : digits.length,
            suffix: suffix ?? '',
        };
    } catch {
        return null;
    }
}

function formatToken(token: ParsedNumericToken, value: bigint): string {
    const negative = value < 0n;
    const abs = negative ? -value : value;
    const raw = abs.toString();
    const padded = raw.padStart(token.width, '0');
    return `${token.prefix}${negative ? '-' : ''}${padded}${token.suffix}`;
}

function detectStep(seeds: string[]): bigint | null {
    if (seeds.length < 2) {
        return 1n;
    }
    const parsed = seeds.map(parseTrailingNumber);
    if (parsed.some((item) => item === null)) {
        return null;
    }
    const first = parsed[0]!;
    const second = parsed[1]!;
    if (first.prefix !== second.prefix || first.suffix !== second.suffix) {
        return null;
    }
    return second.number - first.number;
}

/**
 * Extends `seeds` to `totalLength` rows.
 * - Numeric / suffix-number sequences use detected step (default +1 from a single seed).
 * - Non-numeric seeds are copied.
 */
export function computeAutoFillSeries(seeds: string[], totalLength: number): string[] {
    if (totalLength <= 0) {
        return [];
    }
    if (seeds.length === 0) {
        return Array.from({ length: totalLength }, () => '');
    }
    if (totalLength <= seeds.length) {
        return seeds.slice(0, totalLength);
    }

    const step = detectStep(seeds);
    const last = seeds[seeds.length - 1]!;
    const lastParsed = parseTrailingNumber(last);

    if (step === null || lastParsed === null) {
        return Array.from({ length: totalLength }, (_, index) => seeds[index % seeds.length]!);
    }

    const result = [...seeds];
    let current = lastParsed.number;
    while (result.length < totalLength) {
        current += step === 0n ? 1n : step;
        result.push(formatToken(lastParsed, current));
    }
    return result;
}
