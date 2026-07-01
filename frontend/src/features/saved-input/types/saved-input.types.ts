export interface SavedInput {
    templateId: number;
    inputData: Record<string, unknown>;
    selectedMasterData: Record<string, { id: number }> | null;
    updatedAt: string;
}
