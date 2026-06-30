/**
 * Triggers a client-side download of XML returned by Preview/Export APIs.
 */
export function resolveXmlDownloadFilename(templateName?: string | null): string {
    const name = templateName?.trim();
    if (!name) {
        return 'generated.xml';
    }

    const sanitized = name.replace(/[/\\:*?"<>|]/g, '_').trim();
    return sanitized ? `${sanitized}.xml` : 'generated.xml';
}

export function downloadXml(xml: string, filename: string): void {
    const blob = new Blob([xml], { type: 'application/xml;charset=utf-8' });
    const url = URL.createObjectURL(blob);

    try {
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = filename;
        anchor.rel = 'noopener';
        anchor.style.display = 'none';
        document.body.appendChild(anchor);
        anchor.click();
        document.body.removeChild(anchor);
    } finally {
        URL.revokeObjectURL(url);
    }
}
