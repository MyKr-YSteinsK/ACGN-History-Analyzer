package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.entity.Tag;
import com.mykr.acgnhistoryanalyzer.repository.TagRepository;
import com.mykr.acgnhistoryanalyzer.response.TagResponse;
import com.mykr.acgnhistoryanalyzer.response.TagWhitelistCleanupResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TagService {

    private static final int MAX_SUBJECT_TAG_COUNT = 12;
    private static final Set<String> SUBJECT_ALLOWED_TAG_NAMES = Set.of(
            "漫画改", "小说改", "游戏改", "原创", "奇幻", "战斗", "冒险", "科幻",
            "悬疑", "校园", "青春", "恋爱", "喜剧", "日常", "治愈", "致郁", "运动",
            "音乐", "偶像", "机战", "异世界", "转生", "后宫", "百合", "乙女",
            "女性向", "历史", "战争", "职场", "美食", "家庭", "社团", "短片",
            "3D", "少儿向", "少年向", "少女向"
    );

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<TagResponse> getTags(String keyword) {
        List<Tag> tags;
        if (keyword == null || keyword.trim().isEmpty()) {
            tags = tagRepository.findByStatusOrderByTagTypeAscNameAsc("NORMAL");
        } else {
            tags = tagRepository.findByNameContainingAndStatusOrderByTagTypeAscNameAsc(
                    keyword.trim(),
                    "NORMAL"
            );
        }

        return tags.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TagWhitelistCleanupResponse cleanupToSubjectWhitelist() {
        List<Tag> allTags = tagRepository.findAll();
        Set<String> allowedNormalizedNames = SUBJECT_ALLOWED_TAG_NAMES.stream()
                .map(this::normalizeTagName)
                .collect(java.util.stream.Collectors.toSet());

        List<Tag> toDelete = allTags.stream()
                .filter(tag -> !allowedNormalizedNames.contains(tag.getNormalizedName()))
                .toList();

        if (!toDelete.isEmpty()) {
            tagRepository.deleteAll(toDelete);
        }

        List<String> deletedNames = toDelete.stream()
                .map(Tag::getName)
                .sorted()
                .toList();

        return new TagWhitelistCleanupResponse(
                allTags.size(),
                allTags.size() - toDelete.size(),
                toDelete.size(),
                deletedNames
        );
    }

    public void syncSubjectTags(Subject subject, List<String> tagNames) {
        List<String> normalizedNames = resolveTagNames(tagNames);
        LinkedHashSet<Tag> resolvedTags = new LinkedHashSet<>();

        for (String tagName : normalizedNames) {
            resolvedTags.add(findOrCreateCustomTag(tagName));
        }

        subject.setStructuredTags(resolvedTags);
    }

    public List<String> resolveAllowedSubjectTagNames(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> resolved = new LinkedHashSet<>();
        for (String tagName : tagNames) {
            String cleaned = cleanTagName(tagName);
            if (!cleaned.isEmpty()) {
                String normalized = normalizeSubjectCommonTag(cleaned);
                if (normalized != null) {
                    resolved.add(normalized);
                }
            }
        }

        return new ArrayList<>(resolved);
    }

    public List<TagResponse> toResponses(Collection<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }

        return tags.stream()
                .sorted(Comparator
                        .comparing(Tag::getTagType)
                        .thenComparing(Tag::getName))
                .map(this::toResponse)
                .toList();
    }

    public void ensureDefaultTags() {
        List<DefaultTagDefinition> definitions = List.of(
                new DefaultTagDefinition("奇幻", "GENRE", "sky"),
                new DefaultTagDefinition("科幻", "GENRE", "sky"),
                new DefaultTagDefinition("校园", "GENRE", "sky"),
                new DefaultTagDefinition("恋爱", "GENRE", "pink"),
                new DefaultTagDefinition("悬疑", "GENRE", "violet"),
                new DefaultTagDefinition("推理", "GENRE", "violet"),
                new DefaultTagDefinition("冒险", "GENRE", "emerald"),
                new DefaultTagDefinition("运动", "GENRE", "emerald"),
                new DefaultTagDefinition("音乐", "GENRE", "amber"),
                new DefaultTagDefinition("历史", "GENRE", "amber"),
                new DefaultTagDefinition("战争", "GENRE", "rose"),
                new DefaultTagDefinition("职场", "GENRE", "slate"),
                new DefaultTagDefinition("机战", "GENRE", "blue"),
                new DefaultTagDefinition("异世界", "GENRE", "violet"),

                new DefaultTagDefinition("战斗", "ELEMENT", "rose"),
                new DefaultTagDefinition("魔法", "ELEMENT", "violet"),
                new DefaultTagDefinition("超能力", "ELEMENT", "sky"),
                new DefaultTagDefinition("时间循环", "ELEMENT", "blue"),
                new DefaultTagDefinition("赛博朋克", "ELEMENT", "cyan"),
                new DefaultTagDefinition("废土", "ELEMENT", "amber"),
                new DefaultTagDefinition("克苏鲁", "ELEMENT", "violet"),
                new DefaultTagDefinition("架空", "ELEMENT", "emerald"),
                new DefaultTagDefinition("世界观", "ELEMENT", "slate"),
                new DefaultTagDefinition("群像", "ELEMENT", "pink"),
                new DefaultTagDefinition("成长", "ELEMENT", "emerald"),

                new DefaultTagDefinition("治愈", "MOOD", "emerald"),
                new DefaultTagDefinition("热血", "MOOD", "rose"),
                new DefaultTagDefinition("黑暗", "MOOD", "slate"),
                new DefaultTagDefinition("致郁", "MOOD", "blue"),
                new DefaultTagDefinition("轻松", "MOOD", "sky"),
                new DefaultTagDefinition("搞笑", "MOOD", "amber"),
                new DefaultTagDefinition("温情", "MOOD", "pink"),
                new DefaultTagDefinition("燃", "MOOD", "rose"),

                new DefaultTagDefinition("日常", "STYLE", "emerald"),
                new DefaultTagDefinition("设定系", "STYLE", "slate"),
                new DefaultTagDefinition("慢热", "STYLE", "amber"),
                new DefaultTagDefinition("电波系", "STYLE", "violet"),
                new DefaultTagDefinition("叙事诡计", "STYLE", "blue"),
                new DefaultTagDefinition("漫画改", "STYLE", "emerald"),
                new DefaultTagDefinition("游戏改", "STYLE", "pink"),
                new DefaultTagDefinition("小说改", "STYLE", "violet"),
                new DefaultTagDefinition("原创", "STYLE", "sky")
        );

        for (DefaultTagDefinition definition : definitions) {
            String normalizedName = normalizeTagName(definition.name());
            tagRepository.findByNormalizedName(normalizedName)
                    .orElseGet(() -> tagRepository.save(new Tag(
                            definition.name(),
                            normalizedName,
                            definition.tagType(),
                            definition.colorKey(),
                            true,
                            "NORMAL"
                    )));
        }
    }

    public List<String> resolveTagNames(List<String> tagNames) {
        LinkedHashSet<String> cleanedNames = new LinkedHashSet<>();

        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        for (String tagName : tagNames) {
            String cleanedName = cleanTagName(tagName);
            if (!cleanedName.isEmpty()) {
                cleanedNames.add(cleanedName);
            }
            if (cleanedNames.size() >= MAX_SUBJECT_TAG_COUNT) {
                break;
            }
        }

        return new ArrayList<>(cleanedNames);
    }

    private Tag findOrCreateCustomTag(String name) {
        String normalizedName = normalizeTagName(name);
        return tagRepository.findByNormalizedName(normalizedName)
                .orElseGet(() -> tagRepository.save(new Tag(
                        name,
                        normalizedName,
                        "CUSTOM",
                        "slate",
                        false,
                        "NORMAL"
                )));
    }

    private String normalizeSubjectCommonTag(String tagName) {
        String lowerTag = tagName.toLowerCase(Locale.ROOT);
        return switch (lowerTag) {
            case "漫画改", "漫改" -> "漫画改";
            case "小说改", "轻小说改", "轻改", "ラノベ改" -> "小说改";
            case "游戏改", "galgame改", "gal改" -> "游戏改";
            case "原创", "原創" -> "原创";
            case "奇幻", "魔法" -> "奇幻";
            case "战斗", "动作", "热血" -> "战斗";
            case "冒险" -> "冒险";
            case "科幻", "sf" -> "科幻";
            case "悬疑", "推理" -> "悬疑";
            case "校园", "学园" -> "校园";
            case "青春" -> "青春";
            case "恋爱", "爱情" -> "恋爱";
            case "喜剧", "搞笑" -> "喜剧";
            case "日常" -> "日常";
            case "治愈" -> "治愈";
            case "致郁" -> "致郁";
            case "运动" -> "运动";
            case "音乐" -> "音乐";
            case "偶像" -> "偶像";
            case "机战", "机器人" -> "机战";
            case "异世界" -> "异世界";
            case "转生" -> "转生";
            case "后宫" -> "后宫";
            case "百合" -> "百合";
            case "乙女" -> "乙女";
            case "女性向" -> "女性向";
            case "历史" -> "历史";
            case "战争" -> "战争";
            case "职场" -> "职场";
            case "美食" -> "美食";
            case "家庭" -> "家庭";
            case "社团" -> "社团";
            case "泡面番", "短片" -> "短片";
            case "3d", "3dcg" -> "3D";
            case "子供向", "少儿" -> "少儿向";
            case "少年向" -> "少年向";
            case "少女向" -> "少女向";
            default -> SUBJECT_ALLOWED_TAG_NAMES.contains(tagName) ? tagName : null;
        };
    }

    private String cleanTagName(String tagName) {
        if (tagName == null) {
            return "";
        }

        String cleaned = tagName.trim();
        if (cleaned.startsWith("#")) {
            cleaned = cleaned.substring(1).trim();
        }

        return cleaned.length() > 32 ? cleaned.substring(0, 32) : cleaned;
    }

    private String normalizeTagName(String tagName) {
        return cleanTagName(tagName).toLowerCase(Locale.ROOT);
    }

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getName(),
                tag.getTagType(),
                tag.getColorKey(),
                tag.getBuiltin(),
                tag.getStatus(),
                tag.getCreatedAt(),
                tag.getUpdatedAt()
        );
    }

    private record DefaultTagDefinition(String name, String tagType, String colorKey) {
    }
}
