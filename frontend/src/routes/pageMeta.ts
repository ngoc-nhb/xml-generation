export interface PageMeta {
    title: string;
    description: string;
}

interface RouteMetaRule {
    pattern: RegExp;
    meta: PageMeta;
}

const ROUTE_META_RULES: RouteMetaRule[] = [
    {
        pattern: /^\/templates$/,
        meta: {
            title: 'Templates',
            description: 'Manage XML templates and field definitions.',
        },
    },
    {
        pattern: /^\/templates\/new$/,
        meta: {
            title: 'Create Template',
            description: 'Define template metadata before configuring the schema.',
        },
    },
    {
        pattern: /^\/templates\/import$/,
        meta: {
            title: 'Review Imported Template',
            description: 'Review fields extracted from the uploaded XML before saving.',
        },
    },
    {
        pattern: /^\/templates\/\d+\/schema$/,
        meta: {
            title: 'Template Schema',
            description: 'Configure fields, mappings, and structure for this template.',
        },
    },
    {
        pattern: /^\/templates\/\d+\/edit$/,
        meta: {
            title: 'Edit Template',
            description: 'Update template metadata and status.',
        },
    },
    {
        pattern: /^\/templates\/\d+$/,
        meta: {
            title: 'Template Detail',
            description: 'View template metadata and open the schema editor.',
        },
    },
    {
        pattern: /^\/master-data$/,
        meta: {
            title: 'Master Data',
            description: 'Create reusable master data for XML generation.',
        },
    },
    {
        pattern: /^\/master-data\/types\/\d+\/fields$/,
        meta: {
            title: 'Data Field',
            description: 'Define the field schema for this master data type.',
        },
    },
    {
        pattern: /^\/master-data\/types\/\d+\/records$/,
        meta: {
            title: 'Records',
            description: 'Create and manage records for this master data type.',
        },
    },
    {
        pattern: /^\/master-data\/types\/\d+\/edit$/,
        meta: {
            title: 'Edit Master Type',
            description: 'Update master data type metadata.',
        },
    },
    {
        pattern: /^\/master-data\/types\/\d+$/,
        meta: {
            title: 'Master Type',
            description: 'Review type metadata and continue the master data workflow.',
        },
    },
    {
        pattern: /^\/administration\/users$/,
        meta: {
            title: 'User Management',
            description: 'Create and manage system user accounts and roles.',
        },
    },
    {
        pattern: /^\/workspaces$/,
        meta: {
            title: 'Workspaces',
            description: 'Manage workspaces and switch between data contexts.',
        },
    },
    {
        pattern: /^\/workspaces\/new$/,
        meta: {
            title: 'Create Workspace',
            description: 'Add a new workspace for templates and master data.',
        },
    },
    {
        pattern: /^\/workspaces\/\d+\/edit$/,
        meta: {
            title: 'Edit Workspace',
            description: 'Update workspace name and settings.',
        },
    },
    {
        pattern: /^\/xml-generation$/,
        meta: {
            title: 'XML Generation',
            description: 'Select a template, provide input JSON and master data, then preview or export XML.',
        },
    },
    {
        pattern: /^\/export-history$/,
        meta: {
            title: 'Export History',
            description: 'View previously generated XML files.',
        },
    },
    {
        pattern: /^\/settings$/,
        meta: {
            title: 'Settings',
            description: 'Account and session settings.',
        },
    },
    {
        pattern: /^\/workspace-required$/,
        meta: {
            title: 'Workspace Required',
            description: 'Select a workspace before using application features.',
        },
    },
    {
        pattern: /^\/access-denied$/,
        meta: {
            title: 'Access Denied',
            description: 'You do not have permission to view this page.',
        },
    },
];

const DEFAULT_META: PageMeta = {
    title: 'XMLGen',
    description: 'Template-driven XML generation.',
};

export function resolvePageMeta(pathname: string): PageMeta {
    for (const rule of ROUTE_META_RULES) {
        if (rule.pattern.test(pathname)) {
            return rule.meta;
        }
    }
    return DEFAULT_META;
}
