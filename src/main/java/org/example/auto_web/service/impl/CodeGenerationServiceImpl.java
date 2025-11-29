package org.example.auto_web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.auto_web.pojo.dto.CodeGenerationRequest;
import org.example.auto_web.pojo.other.CodeFileResponse;
import org.example.auto_web.pojo.other.OperationStep;
import org.example.auto_web.service.CodeGenerationService;
import org.example.auto_web.service.CodeGenerationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CodeGenerationServiceImpl implements CodeGenerationService {

    private final Map<String, CodeGenerationStrategy> strategyMap;

    @Autowired
    public CodeGenerationServiceImpl(List<CodeGenerationStrategy> strategies) {
        this.strategyMap = new HashMap<>();
        for (CodeGenerationStrategy strategy : strategies) {
            strategyMap.put(strategy.getLanguage().toLowerCase(), strategy);
        }
    }

    @Override
    public CodeFileResponse generateCodeFile(CodeGenerationRequest request) {
        try {
            // 验证请求
            String validationResult = validateRequest(request);
            if (!"验证通过".equals(validationResult)) {
                throw new IllegalArgumentException(validationResult);
            }

            // 获取对应的策略
            CodeGenerationStrategy strategy = strategyMap.get(request.getLanguage().toLowerCase());
            if (strategy == null) {
                throw new IllegalArgumentException("不支持的语言: " + request.getLanguage());
            }

            // 转换步骤为通用格式
            List<Map<String, Object>> steps = convertStepsToMap(request.getSteps());

            // 生成代码
            String codeContent = strategy.generateCode(steps, request.getInitialUrl(),
                    request.getClassName(), request.getIncludeComments());

            // 生成文件名
            String fileName = request.getClassName() + strategy.getFileExtension();

            // 返回文件响应
            return new CodeFileResponse(
                    fileName,
                    codeContent,
                    request.getLanguage(),
                    (long) codeContent.getBytes().length
            );

        } catch (Exception e) {
            log.error("生成代码文件失败", e);
            throw new RuntimeException("生成代码文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String validateRequest(CodeGenerationRequest request) {
        if (request == null) {
            return "请求不能为空";
        }

        if (request.getLanguage() == null || !strategyMap.containsKey(request.getLanguage().toLowerCase())) {
            return "不支持的语言类型，支持的语言: " + String.join(", ", getSupportedLanguages());
        }

        if (request.getSteps() == null || request.getSteps().isEmpty()) {
            return "操作步骤不能为空";
        }

        if (request.getClassName() == null || request.getClassName().trim().isEmpty()) {
            return "类名/文件名不能为空";
        }

        // 验证步骤内容
        for (int i = 0; i < request.getSteps().size(); i++) {
            OperationStep step = request.getSteps().get(i);
            if (step.getType() == null) {
                return "第 " + (i + 1) + " 步操作类型不能为空";
            }
        }

        return "验证通过";
    }

    @Override
    public List<String> getSupportedLanguages() {
        return new ArrayList<>(strategyMap.keySet());
    }

    private List<Map<String, Object>> convertStepsToMap(List<OperationStep> steps) {
        return steps.stream()
                .map(this::convertStepToMap)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertStepToMap(OperationStep step) {
        Map<String, Object> stepMap = new HashMap<>();
        stepMap.put("type", step.getType().name());
        stepMap.put("xpath", step.getXpath());
        stepMap.put("value", step.getValue());
        stepMap.put("startIndex", step.getStartIndex());
        stepMap.put("endIndex", step.getEndIndex());
        stepMap.put("iterations", step.getIterations());
        stepMap.put("filePath", step.getFilePath());
        stepMap.put("waitBeforeMs", step.getWaitBeforeMs());
        stepMap.put("waitAfterMs", step.getWaitAfterMs());
        stepMap.put("acceptAlert", step.getAcceptAlert());
        stepMap.put("alertText", step.getAlertText());
        stepMap.put("remark", step.getRemark());

        // 处理子步骤
        if (step.getSubSteps() != null && !step.getSubSteps().isEmpty()) {
            List<Map<String, Object>> subSteps = step.getSubSteps().stream()
                    .map(this::convertStepToMap)
                    .collect(Collectors.toList());
            stepMap.put("subSteps", subSteps);
        }

        return stepMap;
    }
}