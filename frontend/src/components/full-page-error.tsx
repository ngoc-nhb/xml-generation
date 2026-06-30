import { AlertTriangle } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

interface FullPageErrorProps {
    title?: string;
    description?: string;
    onRetry?: () => void;
}

export function FullPageError({
    title = 'Something went wrong',
    description = 'An unexpected error occurred. Please try again.',
    onRetry,
}: FullPageErrorProps) {
    return (
        <div className="flex min-h-[50vh] items-center justify-center p-6">
            <Card className="w-full max-w-md">
                <CardHeader className="text-center">
                    <div className="mx-auto mb-2 flex h-12 w-12 items-center justify-center rounded-full bg-red-50">
                        <AlertTriangle className="h-6 w-6 text-destructive" />
                    </div>
                    <CardTitle>{title}</CardTitle>
                    <CardDescription>{description}</CardDescription>
                </CardHeader>
                {onRetry ? (
                    <CardContent className="flex justify-center">
                        <Button onClick={onRetry}>Try again</Button>
                    </CardContent>
                ) : null}
            </Card>
        </div>
    );
}
