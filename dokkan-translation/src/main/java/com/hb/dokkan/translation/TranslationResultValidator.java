package com.hb.dokkan.translation;

import com.hb.dokkan.translation.domain.TranslationItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 校验翻译返回的不可变结构，避免损坏占位符、标签和数值。 */
@Component
public class TranslationResultValidator {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{[^{}]*}");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("</?[^>]+>");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(?:[.,]\\d+)?%?");

    /**
     * 检查一批译文数量及每条译文的结构约束。
     *
     * @param items   原文项
     * @param targets provider 返回译文
     * @return 每项校验错误；空列表表示通过
     */
    public List<List<String>> validate(List<TranslationItem> items, List<String> targets) {
        List<List<String>> errors = new ArrayList<>(items.size());
        for (int index = 0; index < items.size(); index++) {
            List<String> itemErrors = new ArrayList<>();
            if (targets == null || index >= targets.size()) {
                itemErrors.add("missing_translation");
            } else {
                String source = items.get(index).sourceText();
                String target = targets.get(index);
                if (StringUtils.isBlank(target)) {
                    itemErrors.add("empty_translation");
                } else {
                    compareTokens(source, target, PLACEHOLDER_PATTERN, "placeholder_mismatch", itemErrors);
                    compareTokens(source, target, HTML_TAG_PATTERN, "tag_mismatch", itemErrors);
                    compareTokens(source, target, NUMBER_PATTERN, "number_mismatch", itemErrors);
                }
            }
            if (index == items.size() - 1 && targets != null && targets.size() > items.size()) {
                itemErrors.add("unexpected_translation_count");
            }
            errors.add(List.copyOf(itemErrors));
        }
        return List.copyOf(errors);
    }

    /**
     * 比较原文和译文中的指定不可变 token 集合。
     *
     * @param source      原文
     * @param target      译文
     * @param pattern     token 匹配规则
     * @param error       校验失败代码
     * @param itemErrors  当前项错误列表
     */
    private void compareTokens(String source, String target, Pattern pattern, String error,
                               List<String> itemErrors) {
        if (!tokens(source, pattern).equals(tokens(target, pattern))) {
            itemErrors.add(error);
        }
    }

    /**
     * 提取文本中的 token，并按出现顺序保留重复项。
     *
     * @param text    输入文本
     * @param pattern token 匹配规则
     * @return token 列表
     */
    private List<String> tokens(String text, Pattern pattern) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = pattern.matcher(StringUtils.defaultString(text));
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }
}
