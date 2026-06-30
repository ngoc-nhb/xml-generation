import { Plus, Trash2 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select } from '@/components/ui/select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import type { TemplateField, TemplateMapping } from '@/features/templates/types/template.types';

interface SchemaMappingEditorProps {
    fields: TemplateField[];
    mappings: TemplateMapping[];
    onChange: (mappings: TemplateMapping[]) => void;
}

export function SchemaMappingEditor({ fields, mappings, onChange }: SchemaMappingEditorProps) {
    function addMapping() {
        const firstField = fields[0];
        onChange([
            ...mappings,
            {
                fieldName: firstField?.fieldName ?? '',
                masterDataFieldId: null,
            },
        ]);
    }

    function updateMapping(index: number, nextMapping: TemplateMapping) {
        onChange(mappings.map((mapping, mappingIndex) => (mappingIndex === index ? nextMapping : mapping)));
    }

    function removeMapping(index: number) {
        onChange(mappings.filter((_, mappingIndex) => mappingIndex !== index));
    }

    return (
        <div className="space-y-3">
            <div className="flex items-center justify-between">
                <h2 className="text-sm font-semibold text-foreground">Mappings</h2>
                <Button type="button" variant="outline" size="sm" onClick={addMapping} disabled={fields.length === 0}>
                    <Plus className="h-4 w-4" />
                    Add mapping
                </Button>
            </div>
            {fields.length === 0 ? (
                <p className="text-sm text-muted-foreground">Add fields before creating mappings.</p>
            ) : (
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Field</TableHead>
                            <TableHead>Master data field ID</TableHead>
                            <TableHead className="w-16" />
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {mappings.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={3} className="text-muted-foreground">
                                    No mappings defined.
                                </TableCell>
                            </TableRow>
                        ) : (
                            mappings.map((mapping, index) => (
                                <TableRow key={`${mapping.fieldName}-${index}`}>
                                    <TableCell>
                                        <Select
                                            value={mapping.fieldName}
                                            onChange={(event) =>
                                                updateMapping(index, { ...mapping, fieldName: event.target.value })
                                            }
                                        >
                                            {fields.map((field) => (
                                                <option key={field.fieldName} value={field.fieldName}>
                                                    {field.fieldName}
                                                </option>
                                            ))}
                                        </Select>
                                    </TableCell>
                                    <TableCell>
                                        <Input
                                            type="number"
                                            value={mapping.masterDataFieldId ?? ''}
                                            onChange={(event) =>
                                                updateMapping(index, {
                                                    ...mapping,
                                                    masterDataFieldId: event.target.value
                                                        ? Number(event.target.value)
                                                        : null,
                                                })
                                            }
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <Button type="button" variant="ghost" size="sm" onClick={() => removeMapping(index)}>
                                            <Trash2 className="h-4 w-4 text-destructive" />
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            )}
        </div>
    );
}
