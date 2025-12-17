package com.hb.dokkan.config.es;

import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.elasticsearch.indices.IndexSettingsAnalysis;
import org.dromara.easyes.annotation.rely.DefaultSettingsProvider;


/**
 * 逗号分析器设置提供器
 *
 * @author huangbiao
 * @date 2025/12/16 10:51
 **/

public class CommaAnalyzerSettingProvider extends DefaultSettingsProvider {

    private static final String COMMA_ANALYZER = "comma_analyzer";
    private static final String COMMA_TOKENIZER = "comma_tokenizer";

    @Override
    public void settings(IndexSettings.Builder builder) {
        builder.analysis(buildAnalysis());
    }

    private IndexSettingsAnalysis buildAnalysis() {
        IndexSettingsAnalysis analysis = new IndexSettingsAnalysis.Builder()
                // 分析器
                .analyzer(COMMA_ANALYZER, commaAnalyzerBuilder -> {
                    commaAnalyzerBuilder.custom(tokenizerBuilder -> {
                        tokenizerBuilder.tokenizer(COMMA_TOKENIZER).filter("trim");
                        return tokenizerBuilder;
                    });
                    return commaAnalyzerBuilder;
                })
                // 分词器
                .tokenizer(COMMA_TOKENIZER, commaTokenizerBuilder -> {
                    commaTokenizerBuilder.name(COMMA_TOKENIZER);
                    commaTokenizerBuilder.definition(tokenizerDefinitionBuilder -> {
                        tokenizerDefinitionBuilder.pattern(patternTokenizer -> {
                            patternTokenizer.pattern(",").group(-1);
                            return patternTokenizer;
                        });
                        return tokenizerDefinitionBuilder;
                    });
                    return commaTokenizerBuilder;
                })
                .build();
        return analysis;
    }
}