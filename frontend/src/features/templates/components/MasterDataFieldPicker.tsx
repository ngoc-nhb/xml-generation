import { useEffect, useId, useMemo, useRef, useState } from 'react';
import { ChevronDown } from 'lucide-react';
import { Link } from 'react-router-dom';

import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { LoadingSpinner } from '@/components/loading-spinner';
import {
    useMasterDataFieldDetail,
    useMasterDataFieldList,
    useMasterDataTypeList,
} from '@/features/master-data';
import {
    buildMasterDataFieldGroups,
    filterMasterDataFieldGroups,
} from '@/features/templates/utils/groupedMasterDataFields';
import { cn } from '@/utils/cn';

interface MasterDataFieldPickerProps {
    value: number | null;
    onChange: (masterDataFieldId: number | null) => void;
    disabled?: boolean;
}

export function MasterDataFieldPicker({ value, onChange, disabled = false }: MasterDataFieldPickerProps) {
    const [open, setOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const containerRef = useRef<HTMLDivElement>(null);
    const listboxId = useId();

    const { data: typesData, isLoading: typesLoading } = useMasterDataTypeList({ page: 1, pageSize: 500 });
    const { data: fieldsData, isLoading: fieldsLoading } = useMasterDataFieldList({ page: 1, pageSize: 500 });
    const { data: selectedField } = useMasterDataFieldDetail(value ?? undefined);

    const types = typesData?.items ?? [];
    const fields = fieldsData?.items ?? [];
    const isLoading = typesLoading || fieldsLoading;

    const groups = useMemo(() => buildMasterDataFieldGroups(types, fields), [fields, types]);
    const filteredGroups = useMemo(
        () => filterMasterDataFieldGroups(groups, searchQuery),
        [groups, searchQuery],
    );

    const selectedLabel = useMemo(() => {
        if (!value) {
            return null;
        }
        const fromList = fields.find((field) => field.id === value);
        if (fromList) {
            return fromList.name;
        }
        return selectedField?.name ?? null;
    }, [fields, selectedField?.name, value]);

    useEffect(() => {
        if (!open) {
            return;
        }

        function handlePointerDown(event: MouseEvent) {
            if (!containerRef.current?.contains(event.target as Node)) {
                setOpen(false);
                setSearchQuery('');
            }
        }

        document.addEventListener('mousedown', handlePointerDown);
        return () => document.removeEventListener('mousedown', handlePointerDown);
    }, [open]);

    function handleSelect(fieldId: number) {
        onChange(fieldId);
        setOpen(false);
        setSearchQuery('');
    }

    if (isLoading) {
        return <LoadingSpinner label="Loading master data…" />;
    }

    if (types.length === 0) {
        return (
            <div className="space-y-3 rounded-md border border-dashed border-border p-4 text-sm">
                <p className="text-muted-foreground">No Master Data available.</p>
                <p className="text-muted-foreground">Create a Master Data Type first.</p>
                <Button asChild variant="outline" size="sm">
                    <Link to="/master-data">Create Master Data</Link>
                </Button>
            </div>
        );
    }

    if (groups.length === 0) {
        return (
            <div className="space-y-3 rounded-md border border-dashed border-border p-4 text-sm">
                <p className="text-muted-foreground">No master data fields available.</p>
                <p className="text-muted-foreground">Add fields to your Master Data types first.</p>
                <Button asChild variant="outline" size="sm">
                    <Link to="/master-data">Manage Master Data</Link>
                </Button>
            </div>
        );
    }

    return (
        <div ref={containerRef} className="relative">
            <button
                type="button"
                disabled={disabled}
                aria-haspopup="listbox"
                aria-expanded={open}
                aria-controls={listboxId}
                className={cn(
                    'flex h-10 w-full items-center justify-between rounded-md border border-input bg-card px-3 py-2 text-sm',
                    'ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring',
                    'disabled:cursor-not-allowed disabled:opacity-50',
                )}
                onClick={() => {
                    if (disabled) {
                        return;
                    }
                    setOpen((current) => !current);
                    if (open) {
                        setSearchQuery('');
                    }
                }}
            >
                <span className={cn('truncate', !selectedLabel && 'text-muted-foreground')}>
                    {selectedLabel ?? 'Select Master Data field'}
                </span>
                <ChevronDown className={cn('h-4 w-4 shrink-0 opacity-50 transition-transform', open && 'rotate-180')} />
            </button>

            {open ? (
                <div
                    id={listboxId}
                    role="listbox"
                    className="absolute z-50 mt-1 w-full rounded-md border border-border bg-card shadow-md"
                >
                    <div className="border-b border-border p-2">
                        <Input
                            autoFocus
                            value={searchQuery}
                            placeholder="Search…"
                            onChange={(event) => setSearchQuery(event.target.value)}
                            aria-label="Search master data fields"
                        />
                    </div>
                    <ul className="max-h-60 overflow-y-auto py-1">
                        {filteredGroups.length === 0 ? (
                            <li className="px-3 py-2 text-sm text-muted-foreground">No matching master data fields.</li>
                        ) : (
                            filteredGroups.map((group) => (
                                <li key={group.typeId}>
                                    <div
                                        role="presentation"
                                        className="px-3 pb-1 pt-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground"
                                    >
                                        {group.typeName}
                                    </div>
                                    <div className="mx-3 mb-2 border-b border-border" role="separator" />
                                    <ul>
                                        {group.fields.map((field) => (
                                            <li key={field.id}>
                                                <button
                                                    type="button"
                                                    role="option"
                                                    aria-selected={field.id === value}
                                                    className={cn(
                                                        'w-full px-3 py-2 text-left text-sm hover:bg-accent hover:text-accent-foreground',
                                                        field.id === value && 'bg-accent/50 font-medium',
                                                    )}
                                                    onClick={() => handleSelect(field.id)}
                                                >
                                                    {field.name}
                                                </button>
                                            </li>
                                        ))}
                                    </ul>
                                </li>
                            ))
                        )}
                    </ul>
                </div>
            ) : null}
        </div>
    );
}
