import { useState } from 'react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { EmptyPlaceholder } from '@/components/empty-placeholder';
import { toast } from '@/providers/ToastProvider';

function ErrorTrigger(): never {
    throw new Error('Intentional error boundary test');
}

export function DashboardPage() {
    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-foreground">Dashboard</h1>
                <p className="text-sm text-muted-foreground">Foundation placeholder — business widgets arrive in later phases.</p>
            </div>
            <Card>
                <CardHeader>
                    <CardTitle>Foundation checks</CardTitle>
                    <CardDescription>Use these controls to verify Phase 6.1 infrastructure.</CardDescription>
                </CardHeader>
                <CardContent className="flex flex-wrap gap-3">
                    <Button onClick={() => toast.success('Toast system is working')}>Show toast</Button>
                    <Button variant="secondary" onClick={() => toast.error('Sample error toast')}>
                        Show error toast
                    </Button>
                    <ErrorTriggerWrapper />
                </CardContent>
            </Card>
            <EmptyPlaceholder
                title="No dashboard widgets yet"
                description="Summary cards and shortcuts will be added with business features."
            />
        </div>
    );
}

function ErrorTriggerWrapper() {
    const [shouldThrow, setShouldThrow] = useState(false);
    if (shouldThrow) {
        return <ErrorTrigger />;
    }
    return (
        <Button variant="destructive" onClick={() => setShouldThrow(true)}>
            Trigger error boundary
        </Button>
    );
}
