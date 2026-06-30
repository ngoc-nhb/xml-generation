import type { ApiError } from '@/types/api/common';
import { getErrorMessage } from '@/utils/errorMessages';

interface PreviewErrorPanelProps {
    errors: ApiError[];
}

export function PreviewErrorPanel({ errors }: PreviewErrorPanelProps) {
    if (errors.length === 0) {
        return null;
    }

    return (
        <div className="rounded-md border border-destructive/30 bg-red-50 p-4">
            <p className="text-sm font-medium text-destructive">Validation errors</p>
            <ul className="mt-2 space-y-1 text-sm text-destructive">
                {errors.map((error, index) => (
                    <li key={`${error.field ?? 'global'}-${error.code}-${index}`}>
                        {error.field ? (
                            <>
                                <span className="font-mono">{error.field}</span>: {getErrorMessage(error)}
                            </>
                        ) : (
                            getErrorMessage(error)
                        )}
                    </li>
                ))}
            </ul>
        </div>
    );
}
