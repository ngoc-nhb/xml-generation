import { useWorkspace } from '@/features/workspace';
import { ExecutionPanel } from '@/features/xml-generation/components/ExecutionPanel';

export function XmlGenerationPage() {
    const { currentWorkspace } = useWorkspace();

    return (
        <div className="-m-6 flex h-[calc(100vh-5.75rem)] min-h-[32rem] flex-col overflow-hidden p-6">
            <ExecutionPanel key={currentWorkspace?.id ?? 'no-workspace'} />
        </div>
    );
}
