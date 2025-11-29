package org.example.auto_web.service;

import java.util.List;
import java.util.Map;

public interface CodeGenerationStrategy {

    /**
     * 生成代码
     */
    String generateCode(List<Map<String, Object>> steps, String initialUrl, String className, Boolean includeComments);

    /**
     * 获取支持的语言
     */
    String getLanguage();

    /**
     * 获取文件扩展名
     */
    String getFileExtension();
}