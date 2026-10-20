package com.hb.dokkan.translation;

import com.hb.dokkan.translation.domain.TranslationItem;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/** 翻译结构校验器测试。 */
public class TranslationResultValidatorTest {

    /** 缺少占位符或数值时应标记译文无效。 */
    @Test
    public void shouldRejectMissingPlaceholdersAndNumbers() {
        TranslationResultValidator validator = new TranslationResultValidator();
        List<List<String>> errors = validator.validate(
                List.of(new TranslationItem("skill", "ATK +50% for 3 turns {0}")),
                List.of("攻击提升，持续回合"));

        assertTrue(errors.getFirst().contains("placeholder_mismatch"));
        assertTrue(errors.getFirst().contains("number_mismatch"));
    }

    /** 返回条目多于请求时应拒绝该批次最后一项。 */
    @Test
    public void shouldRejectUnexpectedResultCount() {
        TranslationResultValidator validator = new TranslationResultValidator();
        List<List<String>> errors = validator.validate(
                List.of(new TranslationItem("one", "one")), List.of("一", "多余"));

        assertTrue(errors.getFirst().contains("unexpected_translation_count"));
    }
}
