import { useWorkspace } from '@/features/workspace';
import { ExecutionPanel } from '@/features/xml-generation/components/ExecutionPanel';

export function XmlGenerationPage() {
    const { currentWorkspace } = useWorkspace();

    return <ExecutionPanel key={currentWorkspace?.id ?? 'no-workspace'} />;
}
