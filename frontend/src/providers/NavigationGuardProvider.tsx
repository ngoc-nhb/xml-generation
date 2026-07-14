import {
    createContext,
    useCallback,
    useContext,
    useMemo,
    useState,
    type ReactNode,
} from 'react';

import { ConfirmDialog } from '@/features/templates/components/ConfirmDialog';

interface NavigationGuardContextValue {
    setDirty: (dirty: boolean) => void;
    /** Runs `action` immediately if clean; otherwise prompts and runs only on Leave. */
    requestLeave: (action: () => void) => void;
}

const NavigationGuardContext = createContext<NavigationGuardContextValue | undefined>(undefined);

export function NavigationGuardProvider({ children }: { children: ReactNode }) {
    const [dirty, setDirty] = useState(false);
    const [pendingAction, setPendingAction] = useState<(() => void) | null>(null);

    const requestLeave = useCallback(
        (action: () => void) => {
            if (!dirty) {
                action();
                return;
            }
            setPendingAction(() => action);
        },
        [dirty],
    );

    const value = useMemo(
        () => ({
            setDirty,
            requestLeave,
        }),
        [requestLeave],
    );

    return (
        <NavigationGuardContext.Provider value={value}>
            {children}
            <ConfirmDialog
                open={pendingAction !== null}
                title="You have unsaved changes."
                description={
                    'Leaving this page will discard all current edits.\n\nDo you want to continue?'
                }
                cancelLabel="Cancel"
                confirmLabel="Leave"
                destructive
                onCancel={() => setPendingAction(null)}
                onConfirm={() => {
                    const action = pendingAction;
                    setPendingAction(null);
                    setDirty(false);
                    action?.();
                }}
            />
        </NavigationGuardContext.Provider>
    );
}

export function useNavigationGuard(): NavigationGuardContextValue {
    const context = useContext(NavigationGuardContext);
    if (!context) {
        throw new Error('useNavigationGuard must be used within NavigationGuardProvider');
    }
    return context;
}
