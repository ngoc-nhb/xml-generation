/**
 * Master Data feature public API.
 *
 * Cross-feature imports must use this module only.
 *
 * Template integration (Phase 6.3.5):
 * SchemaMappingEditor uses useMasterDataFieldPickerOptions(), useMasterDataFieldDetail(),
 * and useMasterDataTypeDetail() via MasterDataFieldPicker.
 */

export { MasterDataFieldListPage } from '@/features/master-data/pages/MasterDataFieldListPage';
export { MasterDataRecordListPage } from '@/features/master-data/pages/MasterDataRecordListPage';
export { MasterDataTypeDetailPage } from '@/features/master-data/pages/MasterDataTypeDetailPage';
export { MasterDataTypeEditPage } from '@/features/master-data/pages/MasterDataTypeEditPage';
export { MasterDataTypeListPage } from '@/features/master-data/pages/MasterDataTypeListPage';

export {
    useMasterDataFieldDetail,
    useMasterDataFieldList,
    useMasterDataFieldPickerOptions,
    useMasterDataFieldsForType,
} from '@/features/master-data/hooks/useMasterDataFields';

export {
    useMasterDataRecordDetail,
    useMasterDataRecordList,
} from '@/features/master-data/hooks/useMasterDataRecords';

export {
    useMasterDataTypeDetail,
    useMasterDataTypeList,
} from '@/features/master-data/hooks/useMasterDataTypes';

export type {
    MasterDataFieldDetail,
    MasterDataFieldListItem,
    MasterDataFieldOption,
    MasterDataRecordDetail,
    MasterDataRecordListItem,
    MasterDataTypeDetail,
    MasterDataTypeListItem,
} from '@/features/master-data/types/master-data.types';
