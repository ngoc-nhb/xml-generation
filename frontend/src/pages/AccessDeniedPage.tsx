import { Link } from 'react-router-dom';

import { FullPageError } from '@/components/full-page-error';
import { Button } from '@/components/ui/button';

export function AccessDeniedPage() {
    return (
        <div className="space-y-4">
            <FullPageError
                title="Access denied"
                description="You do not have permission to view this page."
            />
            <div className="flex justify-center">
                <Button asChild variant="outline">
                    <Link to="/dashboard">Back to dashboard</Link>
                </Button>
            </div>
        </div>
    );
}
