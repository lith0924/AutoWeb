package org.example.auto_web.service;

import org.example.auto_web.pojo.dto.CodeGenerationRequest;
import org.example.auto_web.pojo.other.CodeFileResponse;

import java.util.List;

public interface CodeGenerationService {

    /**
     * 生成Selenium代码文件
     */
    CodeFileResponse generateCodeFile(CodeGenerationRequest request);

    /**
     * 验证代码生成请求
     */
    String validateRequest(CodeGenerationRequest request);

    /**
     * 获取支持的语言列表
     */
    List<String> getSupportedLanguages();
}
