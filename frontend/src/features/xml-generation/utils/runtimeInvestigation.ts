declare global {
    interface Window {
        __XMLGEN_INVESTIGATION__?: Record<string, unknown>;
    }
}

const PREFIX = '[XMLGEN-INVESTIGATION]';

export function logRuntimeCheckpoint(label: string, data: unknown): void {
    const snapshot = JSON.parse(JSON.stringify(data)) as unknown;
    if (typeof window !== 'undefined') {
        window.__XMLGEN_INVESTIGATION__ = {
            ...(window.__XMLGEN_INVESTIGATION__ ?? {}),
            [label]: snapshot,
        };
    }
    console.log(PREFIX, label, snapshot);
}

export function scheduleArrayLength(data: unknown): number | null {
    if (data == null || typeof data !== 'object') {
        return null;
    }
    const scheduleInfo = (data as Record<string, unknown>).ScheduleInfo;
    if (scheduleInfo == null || typeof scheduleInfo !== 'object') {
        return null;
    }
    const schedule = (scheduleInfo as Record<string, unknown>).Schedule;
    return Array.isArray(schedule) ? schedule.length : null;
}

export interface NestedScheduleInfoSummary {
    gameCategoryCount: number;
    scheduleInfoCountsPerCategory: number[];
    scheduleInfoParentPaths: string[];
    flattenedScheduleInfoAtGameSchedule: boolean;
}

function countScheduleInfoItems(value: unknown): number {
    if (Array.isArray(value)) {
        return value.length;
    }
    if (value != null && typeof value === 'object') {
        return 1;
    }
    return 0;
}

/** Summarize nested GameCategory → ScheduleInfo hierarchy from runtime JSON/form objects. */
export function summarizeNestedScheduleInfo(data: unknown): NestedScheduleInfoSummary {
    const scheduleInfoParentPaths: string[] = [];
    let flattenedScheduleInfoAtGameSchedule = false;

    if (data == null || typeof data !== 'object') {
        return {
            gameCategoryCount: 0,
            scheduleInfoCountsPerCategory: [],
            scheduleInfoParentPaths,
            flattenedScheduleInfoAtGameSchedule: false,
        };
    }

    const root = data as Record<string, unknown>;
    let gameSchedule = root.GameSchedule;
    if (Array.isArray(gameSchedule) && gameSchedule.length === 1 && typeof gameSchedule[0] === 'object') {
        gameSchedule = gameSchedule[0];
    }

    if (gameSchedule != null && typeof gameSchedule === 'object' && !Array.isArray(gameSchedule)) {
        if ('ScheduleInfo' in (gameSchedule as Record<string, unknown>)) {
            flattenedScheduleInfoAtGameSchedule = true;
            scheduleInfoParentPaths.push('GameSchedule.ScheduleInfo');
        }
    }

    const categories = Array.isArray(gameSchedule)
        ? gameSchedule
        : gameSchedule != null && typeof gameSchedule === 'object'
          ? (gameSchedule as Record<string, unknown>).GameCategory
          : null;

    const categoryList = Array.isArray(categories) ? categories : categories != null ? [categories] : [];

    const scheduleInfoCountsPerCategory = categoryList.map((category, index) => {
        if (category == null || typeof category !== 'object') {
            return 0;
        }
        const scheduleInfo = (category as Record<string, unknown>).ScheduleInfo;
        if (scheduleInfo !== undefined) {
            scheduleInfoParentPaths.push(`GameSchedule.GameCategory[${index}].ScheduleInfo`);
        }
        return countScheduleInfoItems(scheduleInfo);
    });

    return {
        gameCategoryCount: categoryList.length,
        scheduleInfoCountsPerCategory,
        scheduleInfoParentPaths,
        flattenedScheduleInfoAtGameSchedule,
    };
}

export interface SchemaHierarchySummary {
    gameCategoryParent: string | null;
    scheduleInfoParent: string | null;
    preservesNestedHierarchy: boolean;
    relevantFields: Array<{ fieldName: string; parentFieldName: string | null; occurrenceRule?: string }>;
}

/** Inspect template schema fields for GameCategory / ScheduleInfo parent chain. */
export function summarizeSchemaHierarchy(
    fields: Array<{ fieldName: string; parentFieldName?: string | null; occurrenceRule?: string | null }>,
): SchemaHierarchySummary {
    const gameCategory = fields.find((field) => field.fieldName === 'GameCategory');
    const scheduleInfo = fields.find((field) => field.fieldName === 'ScheduleInfo');

    const gameCategoryParent = gameCategory?.parentFieldName ?? null;
    const scheduleInfoParent = scheduleInfo?.parentFieldName ?? null;
    const preservesNestedHierarchy =
        gameCategoryParent === 'GameSchedule' && scheduleInfoParent === 'GameCategory';

    const relevantFields = fields
        .filter((field) => field.fieldName === 'GameCategory' || field.fieldName === 'ScheduleInfo')
        .map((field) => ({
            fieldName: field.fieldName,
            parentFieldName: field.parentFieldName ?? null,
            occurrenceRule: field.occurrenceRule ?? undefined,
        }));

    return {
        gameCategoryParent,
        scheduleInfoParent,
        preservesNestedHierarchy,
        relevantFields,
    };
}
