import {
    DndContext,
    type DragEndEvent,
    KeyboardSensor,
    PointerSensor,
    closestCenter,
    useSensor,
    useSensors,
} from '@dnd-kit/core';
import {
    SortableContext,
    sortableKeyboardCoordinates,
    useSortable,
    verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { ChevronDown, ChevronRight, Copy, GripVertical, Plus, Trash2 } from 'lucide-react';
import { useMemo, useState } from 'react';

import { Button } from '@/components/ui/button';
import type { DraftFieldTreeNode } from '@/features/templates/types/template.types';
import { cn } from '@/utils/cn';

interface SchemaFieldTreeProps {
    nodes: DraftFieldTreeNode[];
    selectedClientId: string | null;
    onSelect: (clientId: string) => void;
    onAddRoot: () => void;
    onAddChild: (parentClientId: string) => void;
    onReorder: (clientId: string, newIndex: number, parentClientId: string | null) => void;
    onDuplicate: (clientId: string) => void;
    onDelete: (clientId: string) => void;
}

export function SchemaFieldTree({
    nodes,
    selectedClientId,
    onSelect,
    onAddRoot,
    onAddChild,
    onReorder,
    onDuplicate,
    onDelete,
}: SchemaFieldTreeProps) {
    const [collapsedIds, setCollapsedIds] = useState<Set<string>>(new Set());

    const allNodeIds = useMemo(() => collectNodeIds(nodes), [nodes]);
    const sensors = useSensors(
        useSensor(PointerSensor, { activationConstraint: { distance: 6 } }),
        useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
    );

    function toggleCollapsed(clientId: string) {
        setCollapsedIds((current) => {
            const next = new Set(current);
            if (next.has(clientId)) {
                next.delete(clientId);
            } else {
                next.add(clientId);
            }
            return next;
        });
    }

    function expandAll() {
        setCollapsedIds(new Set());
    }

    function collapseAll() {
        setCollapsedIds(new Set(allNodeIds));
    }

    function handleDragEnd(event: DragEndEvent) {
        const { active, over } = event;
        if (!over || active.id === over.id) {
            return;
        }

        const activeParent = active.data.current?.parentClientId as string | null | undefined;
        const overParent = over.data.current?.parentClientId as string | null | undefined;
        if (activeParent !== overParent) {
            return;
        }

        const siblings = findSiblingNodes(nodes, activeParent ?? null);
        const oldIndex = siblings.findIndex((node) => node.field.clientId === active.id);
        const newIndex = siblings.findIndex((node) => node.field.clientId === over.id);
        if (oldIndex < 0 || newIndex < 0) {
            return;
        }

        onReorder(String(active.id), newIndex, activeParent ?? null);
    }

    return (
        <div className="space-y-3">
            <div className="flex flex-wrap items-center justify-between gap-2">
                <h2 className="text-sm font-semibold text-foreground">Field tree</h2>
                <div className="flex flex-wrap gap-2">
                    <Button type="button" variant="outline" size="sm" onClick={expandAll}>
                        Expand all
                    </Button>
                    <Button type="button" variant="outline" size="sm" onClick={collapseAll}>
                        Collapse all
                    </Button>
                    <Button type="button" variant="outline" size="sm" onClick={onAddRoot}>
                        <Plus className="h-4 w-4" />
                        Add root field
                    </Button>
                </div>
            </div>
            {nodes.length === 0 ? (
                <p className="rounded-md border border-dashed border-border p-4 text-sm text-muted-foreground">
                    No fields yet. Add a root field to start building the schema.
                </p>
            ) : (
                <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
                    <SortableSiblingList
                        nodes={nodes}
                        parentClientId={null}
                        depth={0}
                        selectedClientId={selectedClientId}
                        collapsedIds={collapsedIds}
                        onSelect={onSelect}
                        onToggleCollapsed={toggleCollapsed}
                        onAddChild={onAddChild}
                        onDuplicate={onDuplicate}
                        onDelete={onDelete}
                    />
                </DndContext>
            )}
        </div>
    );
}

function SortableSiblingList({
    nodes,
    parentClientId,
    depth,
    selectedClientId,
    collapsedIds,
    onSelect,
    onToggleCollapsed,
    onAddChild,
    onDuplicate,
    onDelete,
}: {
    nodes: DraftFieldTreeNode[];
    parentClientId: string | null;
    depth: number;
    selectedClientId: string | null;
    collapsedIds: Set<string>;
    onSelect: (clientId: string) => void;
    onToggleCollapsed: (clientId: string) => void;
    onAddChild: (parentClientId: string) => void;
    onDuplicate: (clientId: string) => void;
    onDelete: (clientId: string) => void;
}) {
    const itemIds = nodes.map((node) => node.field.clientId);

    return (
        <SortableContext items={itemIds} strategy={verticalListSortingStrategy}>
            <ul className="space-y-1">
                {nodes.map((node) => (
                    <SortableTreeNode
                        key={node.field.clientId}
                        node={node}
                        parentClientId={parentClientId}
                        depth={depth}
                        selectedClientId={selectedClientId}
                        collapsedIds={collapsedIds}
                        onSelect={onSelect}
                        onToggleCollapsed={onToggleCollapsed}
                        onAddChild={onAddChild}
                        onDuplicate={onDuplicate}
                        onDelete={onDelete}
                    />
                ))}
            </ul>
        </SortableContext>
    );
}

function SortableTreeNode({
    node,
    parentClientId,
    depth,
    selectedClientId,
    collapsedIds,
    onSelect,
    onToggleCollapsed,
    onAddChild,
    onDuplicate,
    onDelete,
}: {
    node: DraftFieldTreeNode;
    parentClientId: string | null;
    depth: number;
    selectedClientId: string | null;
    collapsedIds: Set<string>;
    onSelect: (clientId: string) => void;
    onToggleCollapsed: (clientId: string) => void;
    onAddChild: (parentClientId: string) => void;
    onDuplicate: (clientId: string) => void;
    onDelete: (clientId: string) => void;
}) {
    const isSelected = selectedClientId === node.field.clientId;
    const hasChildren = node.children.length > 0;
    const isCollapsed = collapsedIds.has(node.field.clientId);
    const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
        id: node.field.clientId,
        data: { parentClientId },
    });

    const style = {
        transform: CSS.Transform.toString(transform),
        transition,
    };

    return (
        <li ref={setNodeRef} style={style} className={cn(isDragging && 'opacity-60')}>
            <div
                className={cn(
                    'flex items-center gap-2 rounded-md border px-2 py-1.5',
                    isSelected ? 'border-primary bg-accent' : 'border-transparent hover:bg-muted',
                )}
                style={{ marginLeft: `${depth * 12}px` }}
            >
                <button
                    type="button"
                    className="cursor-grab touch-none text-muted-foreground active:cursor-grabbing"
                    aria-label={`Drag ${node.field.fieldName || 'field'}`}
                    {...attributes}
                    {...listeners}
                >
                    <GripVertical className="h-4 w-4" />
                </button>
                {hasChildren ? (
                    <button
                        type="button"
                        className="shrink-0 text-muted-foreground"
                        aria-label={isCollapsed ? 'Expand node' : 'Collapse node'}
                        onClick={() => onToggleCollapsed(node.field.clientId)}
                    >
                        {isCollapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
                    </button>
                ) : (
                    <span className="inline-flex w-4 shrink-0" />
                )}
                <button
                    type="button"
                    className="flex min-w-0 flex-1 items-center gap-2 text-left text-sm"
                    onClick={() => onSelect(node.field.clientId)}
                >
                    <span className="truncate font-medium">{node.field.fieldName || '(unnamed)'}</span>
                    <span className="truncate text-xs text-muted-foreground">{node.field.nodeType}</span>
                </button>
                <div className="flex shrink-0 gap-1">
                    <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="h-7 w-7 p-0"
                        aria-label="Duplicate field"
                        onClick={() => onDuplicate(node.field.clientId)}
                    >
                        <Copy className="h-4 w-4" />
                    </Button>
                    <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="h-7 w-7 p-0"
                        aria-label="Add child field"
                        onClick={() => onAddChild(node.field.clientId)}
                    >
                        <Plus className="h-4 w-4" />
                    </Button>
                    <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="h-7 w-7 p-0"
                        aria-label="Delete field"
                        onClick={() => onDelete(node.field.clientId)}
                    >
                        <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                </div>
            </div>
            {hasChildren && !isCollapsed ? (
                <SortableSiblingList
                    nodes={node.children}
                    parentClientId={node.field.clientId}
                    depth={depth + 1}
                    selectedClientId={selectedClientId}
                    collapsedIds={collapsedIds}
                    onSelect={onSelect}
                    onToggleCollapsed={onToggleCollapsed}
                    onAddChild={onAddChild}
                    onDuplicate={onDuplicate}
                    onDelete={onDelete}
                />
            ) : null}
        </li>
    );
}

function collectNodeIds(nodes: DraftFieldTreeNode[]): string[] {
    const ids: string[] = [];
    for (const node of nodes) {
        ids.push(node.field.clientId);
        ids.push(...collectNodeIds(node.children));
    }
    return ids;
}

function findSiblingNodes(nodes: DraftFieldTreeNode[], parentClientId: string | null): DraftFieldTreeNode[] {
    if (parentClientId === null) {
        return nodes;
    }

    function search(nodeList: DraftFieldTreeNode[]): DraftFieldTreeNode[] | null {
        for (const node of nodeList) {
            if (node.field.clientId === parentClientId) {
                return node.children;
            }
            const nested = search(node.children);
            if (nested) {
                return nested;
            }
        }
        return null;
    }

    return search(nodes) ?? [];
}
