import { Component, type ErrorInfo, type ReactNode } from 'react';

import { FullPageError } from '@/components/full-page-error';

interface ErrorBoundaryProps {
    children: ReactNode;
}

interface ErrorBoundaryState {
    hasError: boolean;
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
    state: ErrorBoundaryState = { hasError: false };

    static getDerivedStateFromError(): ErrorBoundaryState {
        return { hasError: true };
    }

    componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
        console.error('Unhandled UI error', error, errorInfo);
    }

    private handleRetry = (): void => {
        this.setState({ hasError: false });
    };

    render(): ReactNode {
        if (this.state.hasError) {
            return <FullPageError onRetry={this.handleRetry} />;
        }

        return this.props.children;
    }
}
