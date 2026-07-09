import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { useLocation } from 'react-router-dom';

import { resolvePageMeta, type PageMeta } from '@/routes/pageMeta';

interface PageMetaContextValue {
    meta: PageMeta;
    setPageMeta: (meta: PageMeta | null) => void;
}

const PageMetaContext = createContext<PageMetaContextValue | null>(null);

export function PageMetaProvider({ children }: { children: ReactNode }) {
    const location = useLocation();
    const [override, setOverride] = useState<PageMeta | null>(null);
    const routeMeta = useMemo(() => resolvePageMeta(location.pathname), [location.pathname]);

    useEffect(() => {
        setOverride(null);
    }, [location.pathname]);

    useEffect(() => {
        document.title = `${(override ?? routeMeta).title} · XMLGen`;
    }, [override, routeMeta]);

    const value = useMemo(
        () => ({
            meta: override ?? routeMeta,
            setPageMeta: setOverride,
        }),
        [override, routeMeta],
    );

    return <PageMetaContext.Provider value={value}>{children}</PageMetaContext.Provider>;
}

export function usePageMetaContext(): PageMetaContextValue {
    const context = useContext(PageMetaContext);
    if (!context) {
        throw new Error('usePageMetaContext must be used within PageMetaProvider');
    }
    return context;
}

/** Override the global header title/description for the current page. */
export function useSetPageMeta(meta: PageMeta | null) {
    const { setPageMeta } = usePageMetaContext();

    useEffect(() => {
        setPageMeta(meta);
        return () => setPageMeta(null);
    }, [meta?.title, meta?.description, setPageMeta]);
}
