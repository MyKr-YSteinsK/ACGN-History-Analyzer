package com.mykr.acgnhistoryanalyzer.service;

import com.mykr.acgnhistoryanalyzer.entity.Subject;
import com.mykr.acgnhistoryanalyzer.entity.Tag;
import com.mykr.acgnhistoryanalyzer.repository.TagRepository;
import com.mykr.acgnhistoryanalyzer.response.TagResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TagService {

    private static final int MAX_SUBJECT_TAG_COUNT = 12;

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

    public void syncSubjectTags(Subject subject, List<String> tagNames) {
        List<String> normalizedNames = resolveTagNames(tagNames);
        LinkedHashSet<Tag> resolvedTags = new LinkedHashSet<>();

        for (String tagName : normalizedNames) {
            resolvedTags.add(findOrCreateCustomTag(tagName));
        }

        subject.setStructuredTags(resolvedTags);
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
