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
}
