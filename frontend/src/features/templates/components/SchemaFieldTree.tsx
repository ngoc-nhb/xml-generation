import { ChevronDown, ChevronRight, ChevronUp, Plus, Trash2 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import type { DraftFieldTreeNode } from '@/features/templates/types/template.types';
import { cn } from '@/utils/cn';

interface SchemaFieldTreeProps {
    nodes: DraftFieldTreeNode[];
    selectedClientId: string | null;
    onSelect: (clientId: string) => void;
    onAddRoot: () => void;
    onAddChild: (parentClientId: string) => void;
    onMove: (clientId: string, direction: 'up' | 'down') => void;
    onDelete: (clientId: string) => void;
}

export function SchemaFieldTree({
    nodes,
    selectedClientId,
    onSelect,
    onAddRoot,
    onAddChild,
    onMove,
    onDelete,
}: SchemaFieldTreeProps) {
    return (
        <div className="space-y-3">
            <div className="flex items-center justify-between">
                <h2 className="text-sm font-semibold text-foreground">Field tree</h2>
                <Button type="button" variant="outline" size="sm" onClick={onAddRoot}>
                    <Plus className="h-4 w-4" />
                    Add root field
                </Button>
            </div>
            {nodes.length === 0 ? (
                <p className="rounded-md border border-dashed border-border p-4 text-sm text-muted-foreground">
                    No fields yet. Add a root field to start building the schema.
                </p>
            ) : (
                <ul className="space-y-1">
                    {nodes.map((node) => (
                        <TreeNode
                            key={node.field.clientId}
                            node={node}
                            depth={0}
                            selectedClientId={selectedClientId}
                            onSelect={onSelect}
                            onAddChild={onAddChild}
                            onMove={onMove}
                            onDelete={onDelete}
                        />
                    ))}
                </ul>
            )}
        </div>
    );
}

function TreeNode({
    node,
    depth,
    selectedClientId,
    onSelect,
    onAddChild,
    onMove,
    onDelete,
}: {
    node: DraftFieldTreeNode;
    depth: number;
    selectedClientId: string | null;
    onSelect: (clientId: string) => void;
    onAddChild: (parentClientId: string) => void;
    onMove: (clientId: string, direction: 'up' | 'down') => void;
    onDelete: (clientId: string) => void;
}) {
    const isSelected = selectedClientId === node.field.clientId;

    return (
        <li>
            <div
                className={cn(
                    'flex items-center gap-2 rounded-md border px-2 py-1.5',
                    isSelected ? 'border-primary bg-accent' : 'border-transparent hover:bg-muted',
                )}
                style={{ marginLeft: `${depth * 12}px` }}
            >
                <button
                    type="button"
                    className="flex min-w-0 flex-1 items-center gap-2 text-left text-sm"
                    onClick={() => onSelect(node.field.clientId)}
                >
                    <ChevronRight className="h-4 w-4 shrink-0 text-muted-foreground" />
                    <span className="truncate font-medium">{node.field.fieldName || '(unnamed)'}</span>
                    <span className="truncate text-xs text-muted-foreground">{node.field.nodeType}</span>
                </button>
                <div className="flex shrink-0 gap-1">
                    <Button type="button" variant="ghost" size="sm" className="h-7 w-7 p-0" onClick={() => onMove(node.field.clientId, 'up')}>
                        <ChevronUp className="h-4 w-4" />
                    </Button>
                    <Button type="button" variant="ghost" size="sm" className="h-7 w-7 p-0" onClick={() => onMove(node.field.clientId, 'down')}>
                        <ChevronDown className="h-4 w-4" />
                    </Button>
                    <Button type="button" variant="ghost" size="sm" className="h-7 w-7 p-0" onClick={() => onAddChild(node.field.clientId)}>
                        <Plus className="h-4 w-4" />
                    </Button>
                    <Button type="button" variant="ghost" size="sm" className="h-7 w-7 p-0" onClick={() => onDelete(node.field.clientId)}>
                        <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                </div>
            </div>
            {node.children.length > 0 ? (
                <ul className="mt-1 space-y-1">
                    {node.children.map((child) => (
                        <TreeNode
                            key={child.field.clientId}
                            node={child}
                            depth={depth + 1}
                            selectedClientId={selectedClientId}
                            onSelect={onSelect}
                            onAddChild={onAddChild}
                            onMove={onMove}
                            onDelete={onDelete}
                        />
                    ))}
                </ul>
            ) : null}
        </li>
    );
}
