package org.example.auto_web.service.strategy;

import org.example.auto_web.service.CodeGenerationStrategy;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class GoCodeGenerationStrategy implements CodeGenerationStrategy {

    @Override
    public String generateCode(List<Map<String, Object>> steps, String initialUrl, String className, Boolean includeComments) {
        StringBuilder code = new StringBuilder();

        // 包声明和导入
        code.append("package main\n\n");
        code.append("import (\n");
        code.append("    \"fmt\"\n");
        code.append("    \"log\"\n");
        code.append("    \"os\"\n");
        code.append("    \"time\"\n");
        code.append("    \"strings\"\n");
        code.append("    \"io/ioutil\"\n");
        code.append("    \"encoding/json\"\n");
        code.append("    \"strconv\"\n");
        code.append("    \"regexp\"\n");
        code.append("    \n");
        code.append("    \"github.com/tebeka/selenium\"\n");
        code.append("    \"github.com/tebeka/selenium/chrome\"\n");
        code.append(")\n\n");

        // 注释
        if (includeComments) {
            code.append("/*\n");
            code.append(" * 自动生成的Selenium测试程序 - Go语言版本\n");
            code.append(" * 生成时间: ").append(new Date()).append("\n");
            code.append(" * 步骤数量: ").append(steps.size()).append("\n");
            code.append(" */\n\n");
        }

        // 主函数
        code.append("func main() {\n");
        code.append("    // 设置ChromeDriver选项\n");
        code.append("    caps := selenium.Capabilities{\n");
        code.append("        \"browserName\": \"chrome\",\n");
        code.append("    }\n");
        code.append("    \n");
        code.append("    chromeCaps := chrome.Capabilities{\n");
        code.append("        Args: []string{\n");
        code.append("            \"--start-maximized\",\n");
        code.append("            \"--disable-blink-features=AutomationControlled\",\n");
        code.append("        },\n");
        code.append("        ExcludeSwitches: []string{\"enable-automation\"},\n");
        code.append("    }\n");
        code.append("    caps.AddChrome(chromeCaps)\n");
        code.append("    \n");
        code.append("    // 连接WebDriver\n");
        code.append("    wd, err := selenium.NewRemote(caps, \"\")\n");
        code.append("    if err != nil {\n");
        code.append("        log.Fatalf(\"❌ 连接WebDriver失败: %v\", err)\n");
        code.append("    }\n");
        code.append("    defer wd.Quit()\n");
        code.append("    \n");
        code.append("    // 设置隐式等待\n");
        code.append("    wd.SetImplicitWaitTimeout(10 * time.Second)\n");
        code.append("    \n");
        code.append("    context := make(map[string]interface{})\n");
        code.append("    err = executeSteps(wd, context)\n");
        code.append("    if err != nil {\n");
        code.append("        log.Fatalf(\"❌ 执行失败: %v\", err)\n");
        code.append("    }\n");
        code.append("    \n");
        code.append("    fmt.Println(\"🎉 所有操作执行完成\")\n");
        code.append("}\n\n");

        // 执行步骤函数
        code.append("func executeSteps(wd selenium.WebDriver, context map[string]interface{}) error {\n");

        // 初始导航
        if (initialUrl != null && !initialUrl.isEmpty()) {
            code.append("    // 初始导航\n");
            code.append("    err := wd.Get(\"").append(initialUrl).append("\")\n");
            code.append("    if err != nil {\n");
            code.append("        return fmt.Errorf(\"初始导航失败: %v\", err)\n");
            code.append("    }\n");
            code.append("    fmt.Println(\"✅ 初始导航到: ").append(initialUrl).append("\")\n");
            code.append("    safeWait(2000)\n\n");
        }

        // 生成步骤代码
        for (int i = 0; i < steps.size(); i++) {
            Map<String, Object> step = steps.get(i);
            code.append(generateStepCode(step, i + 1, includeComments));
            code.append("\n");
        }

        code.append("    return nil\n");
        code.append("}\n");

        // 辅助方法
        code.append(generateHelperMethods());

        return code.toString();
    }

    private String generateStepCode(Map<String, Object> step, int stepNumber, Boolean includeComments) {
        String type = (String) step.get("type");
        String remark = (String) step.get("remark");
        StringBuilder stepCode = new StringBuilder();

        // 步骤注释
        if (includeComments) {
            stepCode.append("    // 步骤 ").append(stepNumber);
            if (remark != null && !remark.isEmpty()) {
                stepCode.append(": ").append(remark);
            }
            stepCode.append("\n");
        }

        // 操作前等待
        Long waitBeforeMs = getLongValue(step, "waitBeforeMs");
        if (waitBeforeMs != null && waitBeforeMs > 0) {
            stepCode.append("    safeWait(").append(waitBeforeMs).append(")\n");
        }

        switch (type) {
            case "CLICK":
                stepCode.append(generateClick(step));
                break;
            case "INPUT":
                stepCode.append(generateInput(step));
                break;
            case "GET_TEXT":
                stepCode.append(generateGetText(step));
                break;
            case "LOOP_CLICK":
                stepCode.append(generateLoopClick(step));
                break;
            case "LOOP_INPUT":
                stepCode.append(generateLoopInput(step));
                break;
            case "LOOP_GET_TEXT":
                stepCode.append(generateLoopGetText(step));
                break;
            case "LOOP_TASK":
                stepCode.append(generateLoopTask(step, stepNumber));
                break;
            case "DYNAMIC_LOOP":
                stepCode.append(generateDynamicLoop(step, stepNumber));
                break;
            case "WAIT":
                stepCode.append(generateWait(step));
                break;
            case "NAVIGATE":
                stepCode.append(generateNavigate(step));
                break;
            case "SWITCH_IFRAME":
                stepCode.append(generateSwitchIframe(step));
                break;
            case "GET_CURRENT_URL":
                stepCode.append(generateGetCurrentUrl(step));
                break;
            case "PRESS_KEYS":
                stepCode.append(generatePressKeys(step));
                break;
            case "KEYBOARD_INPUT":
                stepCode.append(generateKeyboardInput(step));
                break;
            case "GO_BACK":
                stepCode.append(generateGoBack(step));
                break;
            case "HANDLE_ALERT":
                stepCode.append(generateHandleAlert(step));
                break;
            case "CLOSE_TAB":
                stepCode.append(generateCloseTab(step));
                break;
            case "IMPORT_COOKIE":
                stepCode.append(generateImportCookie(step));
                break;
            default:
                stepCode.append("    // 不支持的操作类型: ").append(type).append("\n");
        }

        // 操作后等待
        Long waitAfterMs = getLongValue(step, "waitAfterMs");
        if (waitAfterMs != null && waitAfterMs > 0) {
            stepCode.append("    safeWait(").append(waitAfterMs).append(")\n");
        }

        return stepCode.toString();
    }

    private String generateClick(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        return "    resolvedXpath := resolveXpathWithExpression(\"" + xpath + "\", context)\n" +
                "    elem, err := wd.FindElement(selenium.ByXPATH, resolvedXpath)\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"查找元素失败: %v\", err)\n" +
                "    }\n" +
                "    err = elem.Click()\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"点击元素失败: %v\", err)\n" +
                "    }\n" +
                "    fmt.Printf(\"✅ 点击元素: %s\\\\n\", resolvedXpath)\n";
    }

    private String generateInput(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        String value = (String) step.get("value");
        return "    resolvedXpath := resolveXpathWithExpression(\"" + xpath + "\", context)\n" +
                "    resolvedValue := resolveValueWithExpression(\"" + value + "\", context)\n" +
                "    elem, err := wd.FindElement(selenium.ByXPATH, resolvedXpath)\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"查找元素失败: %v\", err)\n" +
                "    }\n" +
                "    err = elem.Clear()\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"清除输入框失败: %v\", err)\n" +
                "    }\n" +
                "    err = elem.SendKeys(resolvedValue)\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"输入内容失败: %v\", err)\n" +
                "    }\n" +
                "    fmt.Printf(\"✅ 输入内容: '%s' 到元素: %s\\\\n\", resolvedValue, resolvedXpath)\n";
    }

    private String generateGetText(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        return "    resolvedXpath := resolveXpathWithExpression(\"" + xpath + "\", context)\n" +
                "    elem, err := wd.FindElement(selenium.ByXPATH, resolvedXpath)\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"查找元素失败: %v\", err)\n" +
                "    }\n" +
                "    text, err := elem.Text()\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"获取文本失败: %v\", err)\n" +
                "    }\n" +
                "    fmt.Printf(\"📖 获取文本: '%s' 从元素: %s\\\\n\", text, resolvedXpath)\n";
    }

    private String generateLoopClick(Map<String, Object> step) {
        Integer startIndex = getIntegerValue(step, "startIndex", 1);
        Integer endIndex = getIntegerValue(step, "endIndex", 1);
        Integer increment = getIntegerValue(step, "increment", 1);
        String xpath = (String) step.get("xpath");

        return "    fmt.Printf(\"🔄 开始遍历点击操作，范围: " + startIndex + " - " + endIndex + "，增量: " + increment + "\\\\n\")\n" +
                "    for i := " + startIndex + "; i <= " + endIndex + "; i += " + increment + " {\n" +
                "        context[\"i\"] = i\n" +
                "        context[\"index\"] = i\n" +
                "        context[\"current\"] = i\n" +
                "        resolvedXpath := resolveXpathWithExpression(\"" + xpath + "\", context)\n" +
                "        elem, err := wd.FindElement(selenium.ByXPATH, resolvedXpath)\n" +
                "        if err != nil {\n" +
                "            fmt.Printf(\"⚠️ 遍历点击失败，索引: %d, XPath: %s\\\\n\", i, resolvedXpath)\n" +
                "            continue\n" +
                "        }\n" +
                "        err = elem.Click()\n" +
                "        if err != nil {\n" +
                "            fmt.Printf(\"⚠️ 遍历点击失败，索引: %d, XPath: %s\\\\n\", i, resolvedXpath)\n" +
                "            continue\n" +
                "        }\n" +
                "        fmt.Printf(\"✅ 遍历点击成功: 索引: %d, XPath: %s\\\\n\", i, resolvedXpath)\n" +
                "        safeWait(500)\n" +
                "    }\n" +
                "    delete(context, \"i\")\n" +
                "    delete(context, \"index\")\n" +
                "    delete(context, \"current\")\n";
    }

    private String generateLoopInput(Map<String, Object> step) {
        Integer startIndex = getIntegerValue(step, "startIndex", 1);
        Integer endIndex = getIntegerValue(step, "endIndex", 1);
        Integer increment = getIntegerValue(step, "increment", 1);
        String xpath = (String) step.get("xpath");
        String value = (String) step.get("value");

        return "    fmt.Printf(\"🔄 开始遍历输入操作，范围: " + startIndex + " - " + endIndex + "，增量: " + increment + "\\\\n\")\n" +
                "    for i := " + startIndex + "; i <= " + endIndex + "; i += " + increment + " {\n" +
                "        context[\"i\"] = i\n" +
                "        context[\"index\"] = i\n" +
                "        context[\"current\"] = i\n" +
                "        resolvedXpath := resolveXpathWithExpression(\"" + xpath + "\", context)\n" +
                "        resolvedValue := resolveValueWithExpression(\"" + value + "\", context)\n" +
                "        elem, err := wd.FindElement(selenium.ByXPATH, resolvedXpath)\n" +
                "        if err != nil {\n" +
                "            fmt.Printf(\"⚠️ 遍历输入失败，索引: %d, XPath: %s\\\\n\", i, resolvedXpath)\n" +
                "            continue\n" +
                "        }\n" +
                "        err = elem.Clear()\n" +
                "        if err != nil {\n" +
                "            fmt.Printf(\"⚠️ 遍历输入失败，索引: %d, XPath: %s\\\\n\", i, resolvedXpath)\n" +
                "            continue\n" +
                "        }\n" +
                "        err = elem.SendKeys(resolvedValue)\n" +
                "        if err != nil {\n" +
                "            fmt.Printf(\"⚠️ 遍历输入失败，索引: %d, XPath: %s\\\\n\", i, resolvedXpath)\n" +
                "            continue\n" +
                "        }\n" +
                "        fmt.Printf(\"✅ 遍历输入成功: 索引: %d, 值: '%s', XPath: %s\\\\n\", i, resolvedValue, resolvedXpath)\n" +
                "        safeWait(500)\n" +
                "    }\n" +
                "    delete(context, \"i\")\n" +
                "    delete(context, \"index\")\n" +
                "    delete(context, \"current\")\n";
    }

    private String generateLoopGetText(Map<String, Object> step) {
        Integer startIndex = getIntegerValue(step, "startIndex", 1);
        Integer endIndex = getIntegerValue(step, "endIndex", 1);
        Integer increment = getIntegerValue(step, "increment", 1);
        String xpath = (String) step.get("xpath");
        String filePath = (String) step.get("filePath");
        if (filePath == null) {
            filePath = "text_output_" + System.currentTimeMillis() + ".txt";
        }

        return "    fmt.Printf(\"🔄 开始遍历获取文本操作，范围: " + startIndex + " - " + endIndex + "，增量: " + increment + "\\\\n\")\n" +
                "    var textList []string\n" +
                "    for i := " + startIndex + "; i <= " + endIndex + "; i += " + increment + " {\n" +
                "        context[\"i\"] = i\n" +
                "        context[\"index\"] = i\n" +
                "        context[\"current\"] = i\n" +
                "        resolvedXpath := resolveXpathWithExpression(\"" + xpath + "\", context)\n" +
                "        elem, err := wd.FindElement(selenium.ByXPATH, resolvedXpath)\n" +
                "        if err != nil {\n" +
                "            fmt.Printf(\"⚠️ 获取文本失败，索引: %d\\\\n\", i)\n" +
                "            textList = append(textList, \"\")\n" +
                "            continue\n" +
                "        }\n" +
                "        text, err := elem.Text()\n" +
                "        if err != nil {\n" +
                "            fmt.Printf(\"⚠️ 获取文本失败，索引: %d\\\\n\", i)\n" +
                "            textList = append(textList, \"\")\n" +
                "            continue\n" +
                "        }\n" +
                "        text = strings.TrimSpace(text)\n" +
                "        textList = append(textList, text)\n" +
                "        fmt.Printf(\"📖 获取文本 [%d]: '%s'\\\\n\", i, text)\n" +
                "        safeWait(500)\n" +
                "    }\n" +
                "    delete(context, \"i\")\n" +
                "    delete(context, \"index\")\n" +
                "    delete(context, \"current\")\n" +
                "    // 写入文件\n" +
                "    content := strings.Join(textList, \"\\\\n\")\n" +
                "    err := ioutil.WriteFile(\"" + filePath + "\", []byte(content), 0644)\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"写入文件失败: %v\", err)\n" +
                "    }\n" +
                "    fmt.Printf(\"💾 成功将 %d 条文本写入文件: " + filePath + "\\\\n\", len(textList))\n";
    }

    private String generateLoopTask(Map<String, Object> step, int stepNumber) {
        Integer iterations = getIntegerValue(step, "iterations", 1);
        Integer increment = getIntegerValue(step, "increment", 1);
        String loopVar = (String) step.get("value");
        if (loopVar == null) {
            loopVar = "i";
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> subSteps = (List<Map<String, Object>>) step.get("subSteps");

        StringBuilder loopCode = new StringBuilder();
        loopCode.append("    fmt.Printf(\"🔄 开始循环任务，迭代次数: ").append(iterations).append("，增量: ").append(increment).append("\\\\n\")\n");
        loopCode.append("    for ").append(loopVar).append(" := 0; ").append(loopVar).append(" < ").append(iterations).append("; ").append(loopVar).append(" += ").append(increment).append(" {\n");
        loopCode.append("        context[\"").append(loopVar).append("\"] = ").append(loopVar).append("\n");
        loopCode.append("        context[\"i\"] = ").append(loopVar).append("\n");
        loopCode.append("        context[\"index\"] = ").append(loopVar).append("\n");
        loopCode.append("        fmt.Printf(\"🔄 循环任务迭代: %d/").append(iterations).append("\\\\n\", ").append(loopVar).append("+1)\n");

        // 生成子步骤代码
        if (subSteps != null) {
            for (int i = 0; i < subSteps.size(); i++) {
                Map<String, Object> subStep = subSteps.get(i);
                String subStepCode = generateStepCode(subStep, i + 1, false)
                        .replace("    ", "        ");
                loopCode.append(subStepCode).append("\n");
            }
        }

        loopCode.append("    }\n");
        loopCode.append("    delete(context, \"").append(loopVar).append("\")\n");
        loopCode.append("    delete(context, \"i\")\n");
        loopCode.append("    delete(context, \"index\")\n");
        return loopCode.toString();
    }

    private String generateDynamicLoop(Map<String, Object> step, int stepNumber) {
        Integer iterations = getIntegerValue(step, "iterations", 1);
        Integer increment = getIntegerValue(step, "increment", 1);
        String loopVar = (String) step.get("value");
        if (loopVar == null) {
            loopVar = "dynamicIndex";
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> subSteps = (List<Map<String, Object>>) step.get("subSteps");

        StringBuilder loopCode = new StringBuilder();
        loopCode.append("    fmt.Printf(\"🔄 开始动态循环，迭代次数: ").append(iterations).append("，增量: ").append(increment).append("\\\\n\")\n");
        loopCode.append("    for ").append(loopVar).append(" := 0; ").append(loopVar).append(" < ").append(iterations).append("; ").append(loopVar).append(" += ").append(increment).append(" {\n");
        loopCode.append("        context[\"").append(loopVar).append("\"] = ").append(loopVar).append("\n");
        loopCode.append("        context[\"i\"] = ").append(loopVar).append("\n");
        loopCode.append("        context[\"index\"] = ").append(loopVar).append("\n");
        loopCode.append("        fmt.Printf(\"🔄 动态循环迭代: %d/").append(iterations).append("\\\\n\", ").append(loopVar).append("+1)\n");

        // 生成子步骤代码
        if (subSteps != null) {
            for (int i = 0; i < subSteps.size(); i++) {
                Map<String, Object> subStep = subSteps.get(i);
                String subStepCode = generateStepCode(subStep, i + 1, false)
                        .replace("    ", "        ");
                loopCode.append(subStepCode).append("\n");
            }
        }

        loopCode.append("        safeWait(1000)\n");
        loopCode.append("    }\n");
        loopCode.append("    delete(context, \"").append(loopVar).append("\")\n");
        loopCode.append("    delete(context, \"i\")\n");
        loopCode.append("    delete(context, \"index\")\n");
        return loopCode.toString();
    }

    private String generateWait(Map<String, Object> step) {
        Long waitTime = getLongValue(step, "waitBeforeMs");
        if (waitTime == null) {
            waitTime = getLongValue(step, "waitAfterMs");
        }
        if (waitTime == null) {
            waitTime = 1000L;
        }
        return "    fmt.Printf(\"⏳ 等待 %d 毫秒\\\\n\", " + waitTime + ")\n" +
                "    safeWait(" + waitTime + ")\n";
    }

    private String generateNavigate(Map<String, Object> step) {
        String url = (String) step.get("value");
        return "    err := wd.Get(\"" + url + "\")\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"导航失败: %v\", err)\n" +
                "    }\n" +
                "    fmt.Println(\"🌐 导航到: " + url + "\")\n";
    }

    private String generateSwitchIframe(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        return "    resolvedXpath := resolveXpathWithExpression(\"" + xpath + "\", context)\n" +
                "    if strings.ToLower(resolvedXpath) == \"default\" {\n" +
                "        wd.SwitchFrame(nil)\n" +
                "        fmt.Println(\"✅ 已切换回默认内容\")\n" +
                "    } else if strings.ToLower(resolvedXpath) == \"parent\" {\n" +
                "        // Go版本暂不支持直接切换到父级frame\n" +
                "        wd.SwitchFrame(nil)\n" +
                "        fmt.Println(\"✅ 已切换回默认内容\")\n" +
                "    } else {\n" +
                "        iframe, err := wd.FindElement(selenium.ByXPATH, resolvedXpath)\n" +
                "        if err != nil {\n" +
                "            return fmt.Errorf(\"查找iframe失败: %v\", err)\n" +
                "        }\n" +
                "        err = wd.SwitchFrame(iframe)\n" +
                "        if err != nil {\n" +
                "            return fmt.Errorf(\"切换iframe失败: %v\", err)\n" +
                "        }\n" +
                "        fmt.Printf(\"✅ 已切换到iframe: %s\\\\n\", resolvedXpath)\n" +
                "    }\n";
    }

    private String generateGetCurrentUrl(Map<String, Object> step) {
        String key = (String) step.get("value");
        if (key == null) {
            key = "current_url";
        }
        return "    currentUrl, err := wd.CurrentURL()\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"获取当前URL失败: %v\", err)\n" +
                "    }\n" +
                "    context[\"" + key + "\"] = currentUrl\n" +
                "    fmt.Printf(\"🌐 获取当前URL: %s\\\\n\", currentUrl)\n" +
                "    fmt.Printf(\"💾 已保存到上下文: " + key + " = %s\\\\n\", currentUrl)\n";
    }

    private String generatePressKeys(Map<String, Object> step) {
        String keys = (String) step.get("value");
        return "    resolvedValue := resolveValueWithExpression(\"" + keys + "\", context)\n" +
                "    // Go版本组合键支持有限，使用SendKeys模拟\n" +
                "    fmt.Printf(\"⌨️ 模拟按键: %s\\\\n\", resolvedValue)\n" +
                "    // 实际使用时需要根据具体按键实现\n";
    }

    private String generateKeyboardInput(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        String value = (String) step.get("value");
        return "    resolvedXpath := resolveXpathWithExpression(\"" + xpath + "\", context)\n" +
                "    resolvedValue := resolveValueWithExpression(\"" + value + "\", context)\n" +
                "    elem, err := wd.FindElement(selenium.ByXPATH, resolvedXpath)\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"查找元素失败: %v\", err)\n" +
                "    }\n" +
                "    err = elem.Click()\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"点击元素失败: %v\", err)\n" +
                "    }\n" +
                "    // Go版本暂不支持复杂的键盘输入序列\n" +
                "    err = elem.SendKeys(resolvedValue)\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"键盘输入失败: %v\", err)\n" +
                "    }\n" +
                "    fmt.Printf(\"⌨️ 键盘输入: '%s' 到元素: %s\\\\n\", resolvedValue, resolvedXpath)\n";
    }

    private String generateGoBack(Map<String, Object> step) {
        return "    err := wd.Back()\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"返回上一页失败: %v\", err)\n" +
                "    }\n" +
                "    fmt.Println(\"↩️ 返回上一页\")\n";
    }

    private String generateHandleAlert(Map<String, Object> step) {
        Boolean acceptAlert = (Boolean) step.get("acceptAlert");
        String alertText = (String) step.get("alertText");

        StringBuilder alertCode = new StringBuilder();
        alertCode.append("    alert, err := wd.AlertText()\n");
        alertCode.append("    if err != nil {\n");
        alertCode.append("        fmt.Println(\"⚠️ 未检测到弹窗\")\n");
        alertCode.append("    } else {\n");

        if (alertText != null && !alertText.isEmpty()) {
            alertCode.append("        // Go版本暂不支持向alert输入文本\n");
            alertCode.append("        fmt.Printf(\"⌨️ 在弹窗中输入文本: ").append(alertText).append("\\\\n\")\n");
        }

        if (acceptAlert != null) {
            if (acceptAlert) {
                alertCode.append("        err = wd.AcceptAlert()\n");
                alertCode.append("        if err != nil {\n");
                alertCode.append("            return fmt.Errorf(\"接受弹窗失败: %v\", err)\n");
                alertCode.append("        }\n");
                alertCode.append("        fmt.Println(\"✅ 接受弹窗\")\n");
            } else {
                alertCode.append("        err = wd.DismissAlert()\n");
                alertCode.append("        if err != nil {\n");
                alertCode.append("            return fmt.Errorf(\"取消弹窗失败: %v\", err)\n");
                alertCode.append("        }\n");
                alertCode.append("        fmt.Println(\"❌ 取消弹窗\")\n");
            }
        }

        alertCode.append("    }\n");

        return alertCode.toString();
    }

    private String generateCloseTab(Map<String, Object> step) {
        return "    windows, err := wd.WindowHandles()\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"获取窗口句柄失败: %v\", err)\n" +
                "    }\n" +
                "    if len(windows) > 1 {\n" +
                "        err = wd.Close()\n" +
                "        if err != nil {\n" +
                "            return fmt.Errorf(\"关闭标签页失败: %v\", err)\n" +
                "        }\n" +
                "        windows, err = wd.WindowHandles()\n" +
                "        if err != nil {\n" +
                "            return fmt.Errorf(\"获取窗口句柄失败: %v\", err)\n" +
                "        }\n" +
                "        if len(windows) > 0 {\n" +
                "            err = wd.SwitchWindow(windows[0])\n" +
                "            if err != nil {\n" +
                "                return fmt.Errorf(\"切换窗口失败: %v\", err)\n" +
                "            }\n" +
                "            currentUrl, _ := wd.CurrentURL()\n" +
                "            fmt.Println(\"✅ 关闭标签页，切换到新标签页\")\n" +
                "            fmt.Printf(\"🌐 当前URL: %s\\\\n\", currentUrl)\n" +
                "        }\n" +
                "    } else {\n" +
                "        fmt.Println(\"⚠️ 只有一个标签页，无法关闭\")\n" +
                "    }\n";
    }

    private String generateImportCookie(Map<String, Object> step) {
        String filePath = (String) step.get("filePath");
        return "    // 读取Cookie文件\n" +
                "    data, err := ioutil.ReadFile(\"" + filePath + "\")\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"读取Cookie文件失败: %v\", err)\n" +
                "    }\n" +
                "    \n" +
                "    var cookies []map[string]interface{}\n" +
                "    err = json.Unmarshal(data, &cookies)\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"解析Cookie JSON失败: %v\", err)\n" +
                "    }\n" +
                "    \n" +
                "    importedCount := 0\n" +
                "    for _, cookieData := range cookies {\n" +
                "        cookie := &selenium.Cookie{\n" +
                "            Name:   cookieData[\"name\"].(string),\n" +
                "            Value:  cookieData[\"value\"].(string),\n" +
                "            Domain: cookieData[\"domain\"].(string),\n" +
                "            Path:   \"/\",\n" +
                "        }\n" +
                "        if path, ok := cookieData[\"path\"].(string); ok {\n" +
                "            cookie.Path = path\n" +
                "        }\n" +
                "        \n" +
                "        err = wd.AddCookie(cookie)\n" +
                "        if err != nil {\n" +
                "            fmt.Printf(\"⚠️ 导入单个Cookie失败: %s\\\\n\", cookie.Name)\n" +
                "            continue\n" +
                "        }\n" +
                "        importedCount++\n" +
                "        fmt.Printf(\"✅ 导入Cookie: %s\\\\n\", cookie.Name)\n" +
                "    }\n" +
                "    \n" +
                "    fmt.Printf(\"✅ 成功导入 %d 个Cookie\\\\n\", importedCount)\n" +
                "    \n" +
                "    // 刷新页面使Cookie生效\n" +
                "    err = wd.Refresh()\n" +
                "    if err != nil {\n" +
                "        return fmt.Errorf(\"刷新页面失败: %v\", err)\n" +
                "    }\n" +
                "    fmt.Println(\"🔄 已刷新页面使Cookie生效\")\n" +
                "    safeWait(2000)\n";
    }

    private String generateHelperMethods() {
        return "func safeWait(milliseconds int) {\n" +
                "    time.Sleep(time.Duration(milliseconds) * time.Millisecond)\n" +
                "}\n\n" +
                "func resolveXpathWithExpression(xpath string, context map[string]interface{}) string {\n" +
                "    if xpath == \"\" {\n" +
                "        return xpath\n" +
                "    }\n" +
                "    result := xpath\n" +
                "    re := regexp.MustCompile(`\\\\{([^}]+)\\\\}`)\n" +
                "    matches := re.FindAllStringSubmatch(xpath, -1)\n" +
                "    \n" +
                "    for _, match := range matches {\n" +
                "        fullMatch := match[0]\n" +
                "        expression := match[1]\n" +
                "        value := parseExpression(fullMatch, context)\n" +
                "        result = strings.Replace(result, fullMatch, fmt.Sprintf(\"%d\", value), -1)\n" +
                "    }\n" +
                "    return result\n" +
                "}\n\n" +
                "func resolveValueWithExpression(value string, context map[string]interface{}) string {\n" +
                "    if value == \"\" {\n" +
                "        return value\n" +
                "    }\n" +
                "    result := value\n" +
                "    re := regexp.MustCompile(`\\\\{([^}]+)\\\\}`)\n" +
                "    matches := re.FindAllStringSubmatch(value, -1)\n" +
                "    \n" +
                "    for _, match := range matches {\n" +
                "        fullMatch := match[0]\n" +
                "        expression := match[1]\n" +
                "        exprValue := parseExpression(fullMatch, context)\n" +
                "        result = strings.Replace(result, fullMatch, fmt.Sprintf(\"%d\", exprValue), -1)\n" +
                "    }\n" +
                "    return result\n" +
                "}\n\n" +
                "func parseExpression(expression string, context map[string]interface{}) int {\n" +
                "    expr := strings.Trim(expression, \"{}\")\n" +
                "    expr = strings.TrimSpace(expr)\n" +
                "    \n" +
                "    if matched, _ := regexp.MatchString(`^\\\\d+$`, expr); matched {\n" +
                "        value, _ := strconv.Atoi(expr)\n" +
                "        return value\n" +
                "    }\n" +
                "    \n" +
                "    for varName, varValue := range context {\n" +
                "        if strings.HasPrefix(expr, varName) {\n" +
                "            baseValue := int(varValue.(int))\n" +
                "            operatorPart := strings.TrimSpace(expr[len(varName):])\n" +
                "            \n" +
                "            if operatorPart == \"\" {\n" +
                "                return baseValue\n" +
                "            }\n" +
                "            \n" +
                "            if matched, _ := regexp.MatchString(`^[+\\\\-*/]\\\\s*\\\\d+$`, operatorPart); matched {\n" +
                "                operator := operatorPart[0]\n" +
                "                numberStr := strings.TrimSpace(operatorPart[1:])\n" +
                "                number, _ := strconv.Atoi(numberStr)\n" +
                "                \n" +
                "                switch operator {\n" +
                "                case '+':\n" +
                "                    return baseValue + number\n" +
                "                case '-':\n" +
                "                    return baseValue - number\n" +
                "                case '*':\n" +
                "                    return baseValue * number\n" +
                "                case '/':\n" +
                "                    return baseValue / number\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    \n" +
                "    return 1\n" +
                "}\n\n";
    }

    // 辅助方法：安全获取整数值
    private Integer getIntegerValue(Map<String, Object> step, String key, Integer defaultValue) {
        Object value = step.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    // 辅助方法：安全获取长整数值
    private Long getLongValue(Map<String, Object> step, String key) {
        Object value = step.get(key);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String getLanguage() {
        return "go";
    }

    @Override
    public String getFileExtension() {
        return ".go";
    }
}