import {
    useEffect,
    useMemo,
    useRef,
    useState,
    type ClipboardEvent,
    type KeyboardEvent,
    type MouseEvent as ReactMouseEvent,
} from 'react';

import { Button } from '@/components/ui/button';
import {
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import type { TemplateField } from '@/features/templates/types/template.types';
import { computeAutoFillSeries } from '@/features/xml-generation/utils/autoFill';
import type { FormObject, FormScalar } from '@/features/xml-generation/utils/inputFormSchema';
import { cn } from '@/utils/cn';

interface BulkEditDialogProps {
    open: boolean;
    label: string;
    fields: TemplateField[];
    selectedIndices: number[];
    items: FormObject[];
    onClose: () => void;
    onApply: (updates: Array<{ index: number; patch: FormObject }>) => void;
}

type Step = 'fields' | 'grid';

interface CellSelection {
    startRow: number;
    startCol: number;
    endRow: number;
    endCol: number;
}

function scalarToInput(value: unknown): string {
    if (value === null || value === undefined) {
        return '';
    }
    return String(value);
}

function parseCellValue(raw: string, field: TemplateField): FormScalar {
    if (field.valueType === 'BOOLEAN') {
        const normalized = raw.trim().toLowerCase();
        return normalized === 'true' || normalized === '1' || normalized === 'yes';
    }
    if (field.valueType === 'INTEGER' || field.valueType === 'LONG') {
        if (raw.trim() === '') {
            return '';
        }
        const parsed = Number.parseInt(raw, 10);
        return Number.isNaN(parsed) ? raw : parsed;
    }
    if (field.valueType === 'DECIMAL') {
        if (raw.trim() === '') {
            return '';
        }
        const parsed = Number.parseFloat(raw);
        return Number.isNaN(parsed) ? raw : parsed;
    }
    return raw;
}

function normalizeSelection(selection: CellSelection): CellSelection {
    return {
        startRow: Math.min(selection.startRow, selection.endRow),
        endRow: Math.max(selection.startRow, selection.endRow),
        startCol: Math.min(selection.startCol, selection.endCol),
        endCol: Math.max(selection.startCol, selection.endCol),
    };
}

export function BulkEditDialog({
    open,
    label,
    fields,
    selectedIndices,
    items,
    onClose,
    onApply,
}: BulkEditDialogProps) {
    const [step, setStep] = useState<Step>('fields');
    const [selectedFieldNames, setSelectedFieldNames] = useState<string[]>([]);
    const [grid, setGrid] = useState<Record<string, string>>({});
    const [selection, setSelection] = useState<CellSelection | null>(null);
    const [fillPreviewRow, setFillPreviewRow] = useState<number | null>(null);
    const cellRefs = useRef<Record<string, HTMLInputElement | null>>({});
    const selectAllFieldsRef = useRef<HTMLInputElement>(null);
    const fillDraggingRef = useRef(false);
    const selectionRef = useRef<CellSelection | null>(null);
    const fillPreviewRowRef = useRef<number | null>(null);
    const applyAutoFillRef = useRef<(activeSelection: CellSelection, targetRow: number) => void>(
        () => undefined,
    );

    const orderedIndices = useMemo(
        () => [...selectedIndices].sort((a, b) => a - b),
        [selectedIndices],
    );

    const allFieldsSelected = fields.length > 0 && selectedFieldNames.length === fields.length;
    const someFieldsSelected = selectedFieldNames.length > 0 && !allFieldsSelected;

    useEffect(() => {
        if (!open) {
            return;
        }
        setStep('fields');
        setSelectedFieldNames(fields.map((field) => field.fieldName));
        setGrid({});
        setSelection(null);
        setFillPreviewRow(null);
    }, [open, fields]);

    useEffect(() => {
        if (selectAllFieldsRef.current) {
            selectAllFieldsRef.current.indeterminate = someFieldsSelected;
        }
    }, [someFieldsSelected, step]);

    useEffect(() => {
        if (!open || step !== 'grid') {
            return;
        }
        const next: Record<string, string> = {};
        for (const index of orderedIndices) {
            const item = items[index] ?? {};
            for (const fieldName of selectedFieldNames) {
                next[`${index}:${fieldName}`] = scalarToInput(item[fieldName]);
            }
        }
        setGrid(next);
        setSelection(null);
        setFillPreviewRow(null);
    }, [open, step, orderedIndices, items, selectedFieldNames]);

    useEffect(() => {
        selectionRef.current = selection;
    }, [selection]);

    useEffect(() => {
        fillPreviewRowRef.current = fillPreviewRow;
    }, [fillPreviewRow]);

    function applyAutoFill(activeSelection: CellSelection, targetRow: number) {
        const normalized = normalizeSelection(activeSelection);
        const seedStart = normalized.startRow;
        const seedEnd = normalized.endRow;
        const colStart = normalized.startCol;
        const colEnd = normalized.endCol;

        if (targetRow >= seedStart && targetRow <= seedEnd) {
            return;
        }

        setGrid((current) => {
            const next = { ...current };
            for (let col = colStart; col <= colEnd; col += 1) {
                const fieldName = selectedFieldNames[col];
                if (fieldName == null) {
                    continue;
                }
                const seeds: string[] = [];
                for (let row = seedStart; row <= seedEnd; row += 1) {
                    const itemIndex = orderedIndices[row];
                    if (itemIndex == null) {
                        continue;
                    }
                    seeds.push(current[cellKey(itemIndex, fieldName)] ?? '');
                }
                if (seeds.length === 0) {
                    continue;
                }

                if (targetRow > seedEnd) {
                    const total = targetRow - seedStart + 1;
                    const series = computeAutoFillSeries(seeds, total);
                    series.forEach((value, offset) => {
                        const itemIndex = orderedIndices[seedStart + offset];
                        if (itemIndex == null) {
                            return;
                        }
                        next[cellKey(itemIndex, fieldName)] = value;
                    });
                } else if (targetRow < seedStart) {
                    const upwardCount = seedStart - targetRow;
                    const reverseSeeds = [...seeds].reverse();
                    const series = computeAutoFillSeries(
                        reverseSeeds,
                        reverseSeeds.length + upwardCount,
                    );
                    const filled = series.slice(reverseSeeds.length).reverse();
                    filled.forEach((value, offset) => {
                        const itemIndex = orderedIndices[targetRow + offset];
                        if (itemIndex == null) {
                            return;
                        }
                        next[cellKey(itemIndex, fieldName)] = value;
                    });
                }
            }
            return next;
        });

        setSelection({
            startRow: Math.min(seedStart, targetRow),
            endRow: Math.max(seedEnd, targetRow),
            startCol: colStart,
            endCol: colEnd,
        });
    }

    applyAutoFillRef.current = applyAutoFill;

    useEffect(() => {
        if (!open || step !== 'grid') {
            return;
        }

        function handleMouseMove(event: globalThis.MouseEvent) {
            if (!fillDraggingRef.current || !selectionRef.current) {
                return;
            }
            const target = document.elementFromPoint(event.clientX, event.clientY);
            const rowAttr = target?.closest('[data-bulk-row]')?.getAttribute('data-bulk-row');
            if (rowAttr == null) {
                return;
            }
            const row = Number.parseInt(rowAttr, 10);
            if (!Number.isNaN(row)) {
                setFillPreviewRow(row);
            }
        }

        function handleMouseUp() {
            if (!fillDraggingRef.current) {
                return;
            }
            fillDraggingRef.current = false;
            const active = selectionRef.current;
            const previewRow = fillPreviewRowRef.current;
            setFillPreviewRow(null);
            if (active != null && previewRow != null) {
                applyAutoFillRef.current(active, previewRow);
            }
        }

        window.addEventListener('mousemove', handleMouseMove);
        window.addEventListener('mouseup', handleMouseUp);
        return () => {
            window.removeEventListener('mousemove', handleMouseMove);
            window.removeEventListener('mouseup', handleMouseUp);
        };
    }, [open, step]);

    function toggleField(fieldName: string, checked: boolean) {
        setSelectedFieldNames((current) =>
            checked
                ? current.includes(fieldName)
                    ? current
                    : [...current, fieldName]
                : current.filter((name) => name !== fieldName),
        );
    }

    function handleSelectAllFields(checked: boolean) {
        setSelectedFieldNames(checked ? fields.map((field) => field.fieldName) : []);
    }

    function cellKey(rowIndex: number, fieldName: string): string {
        return `${rowIndex}:${fieldName}`;
    }

    function focusCell(row: number, col: number) {
        const fieldName = selectedFieldNames[col];
        const itemIndex = orderedIndices[row];
        if (fieldName == null || itemIndex == null) {
            return;
        }
        cellRefs.current[cellKey(itemIndex, fieldName)]?.focus();
    }

    function selectCell(row: number, col: number, extend: boolean) {
        setSelection((current) => {
            if (extend && current) {
                return {
                    ...current,
                    endRow: row,
                    endCol: col,
                };
            }
            return {
                startRow: row,
                startCol: col,
                endRow: row,
                endCol: col,
            };
        });
    }

    function handlePaste(
        event: ClipboardEvent<HTMLInputElement>,
        startRow: number,
        startCol: number,
    ) {
        const text = event.clipboardData.getData('text');
        if (!text.includes('\t') && !text.includes('\n')) {
            return;
        }
        event.preventDefault();
        const rows = text
            .replace(/\r\n/g, '\n')
            .replace(/\r/g, '\n')
            .split('\n')
            .filter((row, index, all) => !(index === all.length - 1 && row === ''));

        setGrid((current) => {
            const next = { ...current };
            rows.forEach((row, rowOffset) => {
                const cols = row.split('\t');
                cols.forEach((cell, colOffset) => {
                    const targetRow = startRow + rowOffset;
                    const targetCol = startCol + colOffset;
                    const itemIndex = orderedIndices[targetRow];
                    const fieldName = selectedFieldNames[targetCol];
                    if (itemIndex == null || fieldName == null) {
                        return;
                    }
                    next[cellKey(itemIndex, fieldName)] = cell;
                });
            });
            return next;
        });
    }

    function handleKeyDown(
        event: KeyboardEvent<HTMLInputElement>,
        row: number,
        col: number,
    ) {
        if (event.key === 'Enter' || event.key === 'ArrowDown') {
            event.preventDefault();
            focusCell(Math.min(row + 1, orderedIndices.length - 1), col);
        } else if (event.key === 'ArrowUp') {
            event.preventDefault();
            focusCell(Math.max(row - 1, 0), col);
        } else if (event.key === 'ArrowRight' && event.currentTarget.selectionStart === event.currentTarget.value.length) {
            event.preventDefault();
            focusCell(row, Math.min(col + 1, selectedFieldNames.length - 1));
        } else if (event.key === 'ArrowLeft' && event.currentTarget.selectionStart === 0) {
            event.preventDefault();
            focusCell(row, Math.max(col - 1, 0));
        } else if (event.key === 'Tab') {
            event.preventDefault();
            if (event.shiftKey) {
                if (col > 0) {
                    focusCell(row, col - 1);
                } else if (row > 0) {
                    focusCell(row - 1, selectedFieldNames.length - 1);
                }
            } else if (col < selectedFieldNames.length - 1) {
                focusCell(row, col + 1);
            } else if (row < orderedIndices.length - 1) {
                focusCell(row + 1, 0);
            }
        }
    }

    function handleFillHandleMouseDown(event: ReactMouseEvent) {
        event.preventDefault();
        event.stopPropagation();
        fillDraggingRef.current = true;
    }

    function handleApply() {
        const fieldByName = new Map(fields.map((field) => [field.fieldName, field]));
        const updates = orderedIndices.map((index) => {
            const patch: FormObject = {};
            for (const fieldName of selectedFieldNames) {
                const field = fieldByName.get(fieldName);
                if (!field) {
                    continue;
                }
                patch[fieldName] = parseCellValue(grid[cellKey(index, fieldName)] ?? '', field);
            }
            return { index, patch };
        });
        onApply(updates);
        onClose();
    }

    const normalizedSelection = selection ? normalizeSelection(selection) : null;
    const previewNormalized =
        normalizedSelection && fillPreviewRow != null
            ? {
                  startRow: Math.min(normalizedSelection.startRow, fillPreviewRow),
                  endRow: Math.max(normalizedSelection.endRow, fillPreviewRow),
                  startCol: normalizedSelection.startCol,
                  endCol: normalizedSelection.endCol,
              }
            : null;
    const highlight = previewNormalized ?? normalizedSelection;

    return (
        <Dialog
            open={open}
            onOpenChange={(next) => {
                if (!next) {
                    onClose();
                }
            }}
        >
            <DialogContent className="max-w-4xl">
                <DialogHeader>
                    <DialogTitle>
                        {step === 'fields' ? `Bulk Edit — choose fields (${label})` : `Bulk Edit — ${label}`}
                    </DialogTitle>
                </DialogHeader>

                {step === 'fields' ? (
                    <div className="max-h-[50vh] space-y-2 overflow-y-auto">
                        <p className="text-sm text-muted-foreground">
                            Select fields to edit for {orderedIndices.length} selected node
                            {orderedIndices.length === 1 ? '' : 's'}.
                        </p>
                        <label className="flex items-center gap-2 border-b border-border pb-2 text-sm font-medium">
                            <input
                                ref={selectAllFieldsRef}
                                type="checkbox"
                                checked={allFieldsSelected}
                                onChange={(event) => handleSelectAllFields(event.target.checked)}
                            />
                            Select All Fields
                        </label>
                        {fields.map((field) => (
                            <label key={field.fieldName} className="flex items-center gap-2 text-sm">
                                <input
                                    type="checkbox"
                                    checked={selectedFieldNames.includes(field.fieldName)}
                                    onChange={(event) => toggleField(field.fieldName, event.target.checked)}
                                />
                                <span className="font-mono">{field.fieldName}</span>
                                {field.displayName ? (
                                    <span className="text-muted-foreground">({field.displayName})</span>
                                ) : null}
                            </label>
                        ))}
                    </div>
                ) : (
                    <div className="max-h-[60vh] overflow-auto">
                        <p className="mb-2 text-xs text-muted-foreground">
                            Select a cell, then drag the fill handle to auto-fill sequences.
                        </p>
                        <table className="min-w-full border-collapse text-sm">
                            <thead>
                                <tr className="border-b border-border bg-muted/40">
                                    <th className="sticky left-0 bg-muted/40 px-2 py-2 text-left font-medium">Node</th>
                                    {selectedFieldNames.map((fieldName) => (
                                        <th key={fieldName} className="px-2 py-2 text-left font-mono font-medium">
                                            {fieldName}
                                        </th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody>
                                {orderedIndices.map((itemIndex, row) => (
                                    <tr
                                        key={itemIndex}
                                        data-bulk-row={row}
                                        className="border-b border-border"
                                    >
                                        <td className="sticky left-0 bg-background px-2 py-1 font-medium">
                                            #{itemIndex + 1}
                                        </td>
                                        {selectedFieldNames.map((fieldName, col) => {
                                            const inSelection =
                                                highlight != null &&
                                                row >= highlight.startRow &&
                                                row <= highlight.endRow &&
                                                col >= highlight.startCol &&
                                                col <= highlight.endCol;
                                            const isFillHandle =
                                                normalizedSelection != null &&
                                                row === normalizedSelection.endRow &&
                                                col === normalizedSelection.endCol;
                                            return (
                                                <td
                                                    key={fieldName}
                                                    data-bulk-row={row}
                                                    className={cn(
                                                        'relative px-1 py-1',
                                                        inSelection ? 'bg-accent/40' : null,
                                                    )}
                                                >
                                                    <Input
                                                        ref={(element) => {
                                                            cellRefs.current[cellKey(itemIndex, fieldName)] =
                                                                element;
                                                        }}
                                                        className="h-8 min-w-[8rem] font-mono text-xs"
                                                        value={grid[cellKey(itemIndex, fieldName)] ?? ''}
                                                        onChange={(event) =>
                                                            setGrid((current) => ({
                                                                ...current,
                                                                [cellKey(itemIndex, fieldName)]:
                                                                    event.target.value,
                                                            }))
                                                        }
                                                        onFocus={() => selectCell(row, col, false)}
                                                        onMouseDown={(event) =>
                                                            selectCell(row, col, event.shiftKey)
                                                        }
                                                        onPaste={(event) => handlePaste(event, row, col)}
                                                        onKeyDown={(event) => handleKeyDown(event, row, col)}
                                                    />
                                                    {isFillHandle ? (
                                                        <button
                                                            type="button"
                                                            aria-label="Auto fill handle"
                                                            className="absolute bottom-0 right-0 z-10 h-2.5 w-2.5 translate-x-1/4 translate-y-1/4 cursor-crosshair border border-background bg-primary"
                                                            onMouseDown={handleFillHandleMouseDown}
                                                        />
                                                    ) : null}
                                                </td>
                                            );
                                        })}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                <DialogFooter>
                    <Button type="button" variant="outline" onClick={onClose}>
                        Cancel
                    </Button>
                    {step === 'fields' ? (
                        <Button
                            type="button"
                            disabled={selectedFieldNames.length === 0}
                            onClick={() => setStep('grid')}
                        >
                            Continue
                        </Button>
                    ) : (
                        <>
                            <Button type="button" variant="outline" onClick={() => setStep('fields')}>
                                Back
                            </Button>
                            <Button type="button" onClick={handleApply}>
                                Apply Changes
                            </Button>
                        </>
                    )}
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
