import type { ApiError } from '@/types/api/common';

export interface ExecutionRequestBody {
    inputData: Record<string, unknown>;
    /** Record references ({ id }) or expanded field-value scopes from import. */
    selectedMasterData: Record<string, unknown>;
}

export interface PreviewSuccess {
    kind: 'success';
    xml: string;
}

export interface ExecutionValidationFailure {
    kind: 'validation';
    errors: ApiError[];
}

export type PreviewResult = PreviewSuccess | ExecutionValidationFailure;

export type ExportResult = PreviewSuccess | ExecutionValidationFailure;

export interface SelectedMasterDataEntry {
    typeId: number;
    typeCode: string;
    typeName: string;
    recordId: number;
    recordLabel: string;
    /**
     * Field name of the repeatable GROUP this selection is scoped to (e.g. `GameCategory`),
     * or `null` when the mapped field is not inside a repeatable group — the template-level,
     * single-selection case that existed before repeated Master Data contexts were supported.
     */
    groupFieldName: string | null;
    /** 0-based occurrence index within `groupFieldName`. Always 0 when `groupFieldName` is null. */
    occurrenceIndex: number;
}
