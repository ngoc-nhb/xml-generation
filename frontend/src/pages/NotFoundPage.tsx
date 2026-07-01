import { Link } from 'react-router-dom';

import { FullPageError } from '@/components/full-page-error';
import { Button } from '@/components/ui/button';

export function NotFoundPage() {
    return (
        <div className="space-y-4">
            <FullPageError title="Page not found" description="The requested page does not exist." />
            <div className="flex justify-center">
                <Button asChild variant="outline">
                    <Link to="/templates">Back to home</Link>
                </Button>
            </div>
        </div>
    );
}
