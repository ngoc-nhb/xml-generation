import { useState } from 'react';

import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/select';
import {
    EMPTY_JSON,
    formatJson,
    INPUT_JSON_PLACEHOLDER,
    parseJsonObject,
} from '@/features/xml-generation/utils/jsonEditor';

interface JsonInputEditorProps {
    value: string;
    onChange: (value: string) => void;
    onValidationChange?: (message: string | null) => void;
}

export function JsonInputEditor({ value, onChange, onValidationChange }: JsonInputEditorProps) {
    const [localError, setLocalError] = useState<string | null>(null);

    function validate(text: string) {
        const result = parseJsonObject(text);
        const message = result.ok ? null : result.message;
        setLocalError(message);
        onValidationChange?.(message);
        return result;
    }

    return (
        <div className="flex h-full flex-col space-y-2">
            <div className="flex items-center justify-between">
                <p className="text-sm font-medium text-foreground">Input JSON</p>
                <div className="flex gap-2">
                    <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => {
                            const result = formatJson(value);
                            if (result.ok) {
                                onChange(result.formatted);
                                validate(result.formatted);
                            } else {
                                setLocalError(result.message);
                                onValidationChange?.(result.message);
                            }
                        }}
                    >
                        Format
                    </Button>
                    <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => {
                            onChange(EMPTY_JSON);
                            validate(EMPTY_JSON);
                        }}
                    >
                        Reset
                    </Button>
                </div>
            </div>
            <Textarea
                className="min-h-[320px] flex-1 font-mono text-xs"
                value={value}
                placeholder={INPUT_JSON_PLACEHOLDER}
                onChange={(event) => {
                    onChange(event.target.value);
                    if (localError) {
                        validate(event.target.value);
                    }
                }}
                onBlur={(event) => validate(event.target.value)}
            />
            {localError ? <p className="text-sm text-destructive">{localError}</p> : null}
        </div>
    );
}

export function parseInputJson(text: string): Record<string, unknown> | null {
    const result = parseJsonObject(text);
    return result.ok ? result.value : null;
}
