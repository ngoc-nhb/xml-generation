import { EmptyPlaceholder } from '@/components/empty-placeholder';

interface PlaceholderPageProps {
    title: string;
    description: string;
}

export function PlaceholderPage({ title, description }: PlaceholderPageProps) {
    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-foreground">{title}</h1>
                <p className="text-sm text-muted-foreground">{description}</p>
            </div>
            <EmptyPlaceholder title="Coming in a future phase" description="This route skeleton is ready for feature implementation." />
        </div>
    );
}
