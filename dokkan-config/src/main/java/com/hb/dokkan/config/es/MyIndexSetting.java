package com.hb.dokkan.config.es;

import org.dromara.easyes.annotation.IndexSetting;
import org.dromara.easyes.common.constants.AnalyzerConstants;

@Index(settingPath = "comma_analyzer.json") // 指定自定义分析器配置文件
public class MyIndexSetting {
}