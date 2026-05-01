package com.mykr.acgnhistoryanalyzer.common.error;

public final class BusinessErrors {

    private BusinessErrors() {
    }

    public static final BusinessError RECORD_NOT_FOUND =
            new BusinessError(4041, "记录不存在");

    public static final BusinessError RELATED_SUBJECT_NOT_FOUND =
            new BusinessError(4042, "关联作品不存在");

    public static final BusinessError SUBJECT_NOT_FOUND =
            new BusinessError(4043, "作品不存在");

    public static final BusinessError FRANCHISE_NOT_FOUND =
            new BusinessError(4044, "系列不存在");

    public static final BusinessError COLLECTION_NOT_FOUND =
            new BusinessError(4045, "收藏不存在");

    public static final BusinessError SUBJECT_ALREADY_HAS_RECORD =
            new BusinessError(4091, "该作品已有记录，请直接修改原记录");

    public static final BusinessError SUBJECT_DELETE_FORBIDDEN =
            new BusinessError(4092, "该作品已有关联记录或收藏，不能删除");

    public static final BusinessError FRANCHISE_DELETE_FORBIDDEN =
            new BusinessError(4093, "该系列下仍有关联作品，不能删除");

    public static final BusinessError FRANCHISE_NAME_DUPLICATED =
            new BusinessError(4094, "系列名称重复，请修改后再保存");

    public static final BusinessError SUBJECT_UNIQUE_DUPLICATED =
            new BusinessError(4095, "作品来源ID重复，请修改后再保存");

    /** 必填字段缺失（与唯一冲突区分，便于前端与监控分类） */
    public static final BusinessError SUBJECT_SOURCE_ID_REQUIRED =
            new BusinessError(4001, "sourceId 不能为空（作品库仅支持 Bangumi 唯一标识）");
}
