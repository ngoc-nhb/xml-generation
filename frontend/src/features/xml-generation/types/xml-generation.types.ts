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
}
