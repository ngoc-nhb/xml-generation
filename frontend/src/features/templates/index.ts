/**
 * Template feature public API.
 *
 * Cross-feature imports must use this module only.
 * Do not import from internal paths such as api/, utils/, or components/ directly.
 */

export { TemplateCreatePage } from '@/features/templates/pages/TemplateCreatePage';
export { TemplateDetailPage } from '@/features/templates/pages/TemplateDetailPage';
export { TemplateEditPage } from '@/features/templates/pages/TemplateEditPage';
export { TemplateListPage } from '@/features/templates/pages/TemplateListPage';
export { TemplateSchemaEditorPage } from '@/features/templates/pages/TemplateSchemaEditorPage';

export {
    useCreateTemplate,
    useDeleteTemplate,
    useTemplateDetail,
    useTemplateList,
    useUpdateTemplate,
    useUpdateTemplateSchema,
} from '@/features/templates/hooks/useTemplates';

export type {
    CreateTemplateRequest,
    TemplateDetail,
    TemplateField,
    TemplateListItem,
    TemplateListParams,
    TemplateMapping,
    TemplateSchema,
    TemplateStatus,
    UpdateTemplateRequest,
} from '@/features/templates/types/template.types';
