package org.example.auto_web.controller;

import org.example.auto_web.pojo.dto.CookieExportRequest;
import org.example.auto_web.pojo.dto.ExecuteRequest;
import org.example.auto_web.pojo.other.OperationStep;
import org.example.auto_web.service.SeleniumAutoWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auto")
public class AutoWebController {

    @Autowired
    private SeleniumAutoWebService seleniumAutoWebService;

    @PostMapping("/execute")
    public ResponseEntity<String> execute(@RequestBody ExecuteRequest request) {
        try {
            seleniumAutoWebService.executeOperations(request);
            return ResponseEntity.ok("自动化操作执行完成");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("执行失败: " + e.getMessage());
        }
    }


    @PostMapping("/export-cookie")
    public ResponseEntity<String> exportCookie(@RequestBody CookieExportRequest request) {
        try {
            String result = seleniumAutoWebService.exportCookie(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("导出Cookie失败: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateSteps(@RequestBody ExecuteRequest request) {
        try {
            // 验证URL
            if (request.getUrl() == null || request.getUrl().isEmpty()) {
                return ResponseEntity.badRequest().body("目标网址不能为空");
            }

            // 验证步骤列表
            List<OperationStep> steps = request.getSteps();
            if (steps == null || steps.isEmpty()) {
                return ResponseEntity.badRequest().body("操作步骤列表不能为空");
            }

            // 简单的步骤验证逻辑
            for (int i = 0; i < steps.size(); i++) {
                OperationStep step = steps.get(i);
                if (step.getType() == null) {
                    return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步操作类型不能为空");
                }

                // 根据不同类型验证必要参数
                switch (step.getType()) {
                    case CLICK:
                    case GET_TEXT:
                    case SWITCH_IFRAME:
                        if (step.getXpath() == null || step.getXpath().isEmpty()) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步XPath不能为空");
                        }
                        break;
                    case INPUT:
                        if (step.getXpath() == null || step.getXpath().isEmpty()) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步XPath不能为空");
                        }
                        if (step.getValue() == null || step.getValue().isEmpty()) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步输入值不能为空");
                        }
                        break;
                    case KEYBOARD_INPUT:
                        if (step.getXpath() == null || step.getXpath().isEmpty()) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步XPath不能为空");
                        }
                        if (step.getValue() == null || step.getValue().isEmpty()) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步键盘输入内容不能为空");
                        }
                        break;
                    case LOOP_CLICK:
                    case LOOP_INPUT:
                    case LOOP_GET_TEXT:
                        if (step.getXpath() == null || step.getXpath().isEmpty()) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步XPath不能为空");
                        }
                        if (step.getStartIndex() == null || step.getEndIndex() == null) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步循环操作缺少起始或结束索引");
                        }
                        if (step.getStartIndex() > step.getEndIndex()) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步起始索引不能大于结束索引");
                        }
                        break;
                    case LOOP_TASK:
                    case DYNAMIC_LOOP:
                        if (step.getIterations() == null || step.getIterations() <= 0) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步循环次数必须大于0");
                        }
                        if (step.getSubSteps() == null || step.getSubSteps().isEmpty()) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步循环任务子步骤不能为空");
                        }
                        // 递归验证子步骤
                        String subStepValidation = validateSubSteps(step.getSubSteps(), String.valueOf(i + 1));
                        if (!subStepValidation.equals("通过")) {
                            return ResponseEntity.badRequest().body(subStepValidation);
                        }
                        break;
                    case NAVIGATE:
                        if (step.getValue() == null || step.getValue().isEmpty()) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步跳转URL不能为空");
                        }
                        break;
                    case PRESS_KEYS:
                        if (step.getValue() == null || step.getValue().isEmpty()) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步按键序列不能为空");
                        }
                        break;
                    case HANDLE_ALERT:
                        if (step.getAcceptAlert() == null) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步弹窗处理必须指定acceptAlert参数");
                        }
                        break;
                    case IMPORT_COOKIE:
                        if (step.getFilePath() == null || step.getFilePath().isEmpty()) {
                            return ResponseEntity.badRequest().body("第 " + (i + 1) + " 步导入Cookie操作需要指定文件路径");
                        }
                        break;
                    case WAIT:
                    case GET_CURRENT_URL:
                    case GO_BACK:
                    case CLOSE_TAB:
                        // 这些操作可以没有XPath
                        break;
                }
            }
            return ResponseEntity.ok("步骤验证通过");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("验证失败: " + e.getMessage());
        }
    }

    // 递归验证子步骤的方法
    private String validateSubSteps(List<OperationStep> subSteps, String parentStepIndex) {
        for (int i = 0; i < subSteps.size(); i++) {
            OperationStep step = subSteps.get(i);
            if (step.getType() == null) {
                return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步操作类型不能为空";
            }

            switch (step.getType()) {
                case CLICK:
                case GET_TEXT:
                case SWITCH_IFRAME:
                    if (step.getXpath() == null || step.getXpath().isEmpty()) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步XPath不能为空";
                    }
                    break;
                case INPUT:
                    if (step.getXpath() == null || step.getXpath().isEmpty()) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步XPath不能为空";
                    }
                    if (step.getValue() == null || step.getValue().isEmpty()) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步输入值不能为空";
                    }
                    break;
                case KEYBOARD_INPUT:
                    if (step.getXpath() == null || step.getXpath().isEmpty()) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步XPath不能为空";
                    }
                    if (step.getValue() == null || step.getValue().isEmpty()) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步键盘输入内容不能为空";
                    }
                    break;
                case LOOP_CLICK:
                case LOOP_INPUT:
                case LOOP_GET_TEXT:
                    if (step.getXpath() == null || step.getXpath().isEmpty()) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步XPath不能为空";
                    }
                    if (step.getStartIndex() == null || step.getEndIndex() == null) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步循环操作缺少起始或结束索引";
                    }
                    if (step.getStartIndex() > step.getEndIndex()) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步起始索引不能大于结束索引";
                    }
                    break;
                case LOOP_TASK:
                case DYNAMIC_LOOP:
                    if (step.getIterations() == null || step.getIterations() <= 0) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步循环次数必须大于0";
                    }
                    if (step.getSubSteps() == null || step.getSubSteps().isEmpty()) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步循环任务子步骤不能为空";
                    }
                    // 递归验证嵌套子步骤
                    String nestedValidation = validateSubSteps(step.getSubSteps(), parentStepIndex + "." + (i + 1));
                    if (!nestedValidation.equals("通过")) {
                        return nestedValidation;
                    }
                    break;
                case NAVIGATE:
                    if (step.getValue() == null || step.getValue().isEmpty()) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步跳转URL不能为空";
                    }
                    break;
                case PRESS_KEYS:
                    if (step.getValue() == null || step.getValue().isEmpty()) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步按键序列不能为空";
                    }
                    break;
                case HANDLE_ALERT:
                    if (step.getAcceptAlert() == null) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步弹窗处理必须指定acceptAlert参数";
                    }
                    break;
                case IMPORT_COOKIE:
                    if (step.getFilePath() == null || step.getFilePath().isEmpty()) {
                        return "第 " + parentStepIndex + " 步的子步骤第 " + (i + 1) + " 步导入Cookie操作需要指定文件路径";
                    }
                    break;
                case WAIT:
                case GET_CURRENT_URL:
                case GO_BACK:
                case CLOSE_TAB:
                    // 等待操作可以没有XPath
                    break;
            }
        }
        return "通过";
    }
}
