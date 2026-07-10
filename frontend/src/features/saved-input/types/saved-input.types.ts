export interface SavedInput {
    templateId: number;
    inputData: Record<string, unknown>;
    /** Flat `{ typeCode: { id } }` for template-level types, or `{ groupFieldName: [{ typeCode: { id } }, ...] }` for types owned by a repeatable group, aligned by occurrence index with `inputData`. */
    selectedMasterData: Record<string, unknown> | null;
    updatedAt: string;
}
