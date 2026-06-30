import { useEffect } from 'react';
import { useBlocker } from 'react-router-dom';

interface UseUnsavedChangesBlockerOptions {
    when: boolean;
    message?: string;
}

export function useUnsavedChangesBlocker({ when, message = 'You have unsaved changes.' }: UseUnsavedChangesBlockerOptions) {
    const blocker = useBlocker(when);

    useEffect(() => {
        if (!when) {
            return;
        }

        const handleBeforeUnload = (event: BeforeUnloadEvent) => {
            event.preventDefault();
            event.returnValue = message;
        };

        window.addEventListener('beforeunload', handleBeforeUnload);
        return () => window.removeEventListener('beforeunload', handleBeforeUnload);
    }, [when, message]);

    return blocker;
}
