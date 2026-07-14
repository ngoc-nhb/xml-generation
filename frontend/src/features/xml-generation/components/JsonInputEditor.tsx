import { useRef, useState } from 'react';

import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/select';
import {
    EMPTY_JSON,
    formatJson,
    formatJsonValidationMessage,
    INPUT_JSON_PLACEHOLDER,
    parseJsonObject,
    selectTextareaOffset,
} from '@/features/xml-generation/utils/jsonEditor';

interface JsonInputEditorProps {
    value: string;
    onChange: (value: string) => void;
    onValidationChange?: (message: string | null) => void;
}

export function JsonInputEditor({ value, onChange, onValidationChange }: JsonInputEditorProps) {
    const [localError, setLocalError] = useState<string | null>(null);
    const textareaRef = useRef<HTMLTextAreaElement | null>(null);

    function validate(text: string) {
        const result = parseJsonObject(text);
        if (result.ok) {
            setLocalError(null);
            onValidationChange?.(null);
            return result;
        }
        const message = formatJsonValidationMessage(result);
        setLocalError(message);
        onValidationChange?.(message);
        if (result.position != null && textareaRef.current) {
            selectTextareaOffset(textareaRef.current, result.position);
        }
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
                                const message = formatJsonValidationMessage(result);
                                setLocalError(message);
                                onValidationChange?.(message);
                                if (result.position != null && textareaRef.current) {
                                    selectTextareaOffset(textareaRef.current, result.position);
                                }
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
                ref={textareaRef}
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
            {localError ? (
                <pre className="whitespace-pre-wrap rounded-md border border-destructive/30 bg-destructive/5 p-2 text-sm text-destructive">
                    {localError}
                </pre>
            ) : null}
        </div>
    );
}

export function parseInputJson(text: string): Record<string, unknown> | null {
    const result = parseJsonObject(text);
    return result.ok ? result.value : null;
}
