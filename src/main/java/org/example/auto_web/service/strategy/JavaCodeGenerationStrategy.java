package org.example.auto_web.service.strategy;

import org.example.auto_web.pojo.other.OperationStep;
import org.example.auto_web.service.CodeGenerationStrategy;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JavaCodeGenerationStrategy implements CodeGenerationStrategy {

    @Override
    public String generateCode(List<Map<String, Object>> steps, String initialUrl, String className, Boolean includeComments) {
        StringBuilder code = new StringBuilder();

        // 包声明和导入
        code.append("package com.generated.selenium;\n\n");
        code.append("import org.openqa.selenium.*;\n");
        code.append("import org.openqa.selenium.chrome.ChromeDriver;\n");
        code.append("import org.openqa.selenium.chrome.ChromeOptions;\n");
        code.append("import org.openqa.selenium.support.ui.ExpectedConditions;\n");
        code.append("import org.openqa.selenium.support.ui.WebDriverWait;\n");
        code.append("import org.openqa.selenium.interactions.Actions;\n");
        code.append("import java.time.Duration;\n");
        code.append("import java.util.*;\n");
        code.append("import java.io.*;\n");
        code.append("import java.nio.file.*;\n");
        code.append("import java.nio.charset.StandardCharsets;\n");
        code.append("import com.fasterxml.jackson.databind.ObjectMapper;\n");
        code.append("import com.fasterxml.jackson.core.type.TypeReference;\n\n");

        // 类注释
        if (includeComments) {
            code.append("/**\n");
            code.append(" * 自动生成的Selenium测试类\n");
            code.append(" * 生成时间: ").append(new Date()).append("\n");
            code.append(" * 步骤数量: ").append(steps.size()).append("\n");
            code.append(" */\n");
        }

        // 类定义
        code.append("public class ").append(className).append(" {\n\n");

        // 上下文变量声明
        code.append("    private static Map<String, Object> context = new HashMap<>();\n\n");

        // 主方法
        code.append("    public static void main(String[] args) {\n");
        code.append("        // 设置ChromeDriver路径\n");
        code.append("        System.setProperty(\"webdriver.chrome.driver\", \"chromedriver\");\n");
        code.append("        \n");
        code.append("        ChromeOptions options = new ChromeOptions();\n");
        code.append("        options.addArguments(\"--start-maximized\");\n");
        code.append("        options.addArguments(\"--disable-blink-features=AutomationControlled\");\n");
        code.append("        options.setExperimentalOption(\"excludeSwitches\", new String[]{\"enable-automation\"});\n");
        code.append("        WebDriver driver = new ChromeDriver(options);\n");
        code.append("        \n");
        code.append("        try {\n");

        // 初始导航
        if (initialUrl != null && !initialUrl.isEmpty()) {
            code.append("            // 初始导航\n");
            code.append("            driver.get(\"").append(initialUrl).append("\");\n");
            code.append("            System.out.println(\"✅ 初始导航到: ").append(initialUrl).append("\");\n");
            code.append("            Thread.sleep(2000);\n\n");
        }

        // 生成步骤代码
        for (int i = 0; i < steps.size(); i++) {
            Map<String, Object> step = steps.get(i);
            code.append(generateStepCode(step, i + 1, includeComments));
            code.append("\n");
        }

        code.append("            System.out.println(\"🎉 所有操作执行完成\");\n");
        code.append("            \n");
        code.append("        } catch (Exception e) {\n");
        code.append("            System.err.println(\"❌ 执行失败: \" + e.getMessage());\n");
        code.append("            e.printStackTrace();\n");
        code.append("        } finally {\n");
        code.append("            driver.quit();\n");
        code.append("            System.out.println(\"🔚 浏览器已关闭\");\n");
        code.append("        }\n");
        code.append("    }\n\n");

        // 辅助方法
        code.append(generateHelperMethods());

        code.append("}\n");

        return code.toString();
    }

    private String generateStepCode(Map<String, Object> step, int stepNumber, Boolean includeComments) {
        String type = (String) step.get("type");
        String remark = (String) step.get("remark");
        StringBuilder stepCode = new StringBuilder();

        // 步骤注释
        if (includeComments) {
            stepCode.append("            // 步骤 ").append(stepNumber);
            if (remark != null && !remark.isEmpty()) {
                stepCode.append(": ").append(remark);
            }
            stepCode.append("\n");
        }

        // 操作前等待
        Long waitBeforeMs = getLongValue(step, "waitBeforeMs");
        if (waitBeforeMs != null && waitBeforeMs > 0) {
            stepCode.append("            safeWait(").append(waitBeforeMs).append("L);\n");
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
                stepCode.append("            // 不支持的操作类型: ").append(type).append("\n");
        }

        // 操作后等待
        Long waitAfterMs = getLongValue(step, "waitAfterMs");
        if (waitAfterMs != null && waitAfterMs > 0) {
            stepCode.append("            safeWait(").append(waitAfterMs).append("L);\n");
        }

        return stepCode.toString();
    }

    private String generateClick(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        return "            {\n" +
                "                String resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));\n" +
                "                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(resolvedXpath)));\n" +
                "                element.click();\n" +
                "                System.out.println(\"✅ 点击元素: \" + resolvedXpath);\n" +
                "            }\n";
    }

    private String generateInput(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        String value = (String) step.get("value");
        return "            {\n" +
                "                String resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "                String resolvedValue = resolveValueWithExpression(\"" + value + "\", context);\n" +
                "                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));\n" +
                "                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(resolvedXpath)));\n" +
                "                element.clear();\n" +
                "                element.sendKeys(resolvedValue);\n" +
                "                System.out.println(\"✅ 输入内容: '\" + resolvedValue + \"' 到元素: \" + resolvedXpath);\n" +
                "            }\n";
    }

    private String generateGetText(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        return "            {\n" +
                "                String resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));\n" +
                "                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(resolvedXpath)));\n" +
                "                String text = element.getText();\n" +
                "                System.out.println(\"📖 获取文本: '\" + text + \"' 从元素: \" + resolvedXpath);\n" +
                "            }\n";
    }

    private String generateLoopClick(Map<String, Object> step) {
        Integer startIndex = getIntegerValue(step, "startIndex", 1);
        Integer endIndex = getIntegerValue(step, "endIndex", 1);
        Integer increment = getIntegerValue(step, "increment", 1);
        String xpath = (String) step.get("xpath");

        return "            {\n" +
                "                System.out.println(\"🔄 开始遍历点击操作，范围: " + startIndex + " - " + endIndex + "，增量: " + increment + "\");\n" +
                "                for (int i = " + startIndex + "; i <= " + endIndex + "; i += " + increment + ") {\n" +
                "                    context.put(\"i\", i);\n" +
                "                    context.put(\"index\", i);\n" +
                "                    context.put(\"current\", i);\n" +
                "                    String resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));\n" +
                "                    try {\n" +
                "                        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(resolvedXpath)));\n" +
                "                        element.click();\n" +
                "                        System.out.println(\"✅ 遍历点击成功: 索引: \" + i + \", XPath: \" + resolvedXpath);\n" +
                "                        safeWait(500);\n" +
                "                    } catch (Exception e) {\n" +
                "                        System.out.println(\"⚠️ 遍历点击失败，索引: \" + i + \", XPath: \" + resolvedXpath);\n" +
                "                    }\n" +
                "                }\n" +
                "                context.remove(\"i\");\n" +
                "                context.remove(\"index\");\n" +
                "                context.remove(\"current\");\n" +
                "            }\n";
    }

    private String generateLoopInput(Map<String, Object> step) {
        Integer startIndex = getIntegerValue(step, "startIndex", 1);
        Integer endIndex = getIntegerValue(step, "endIndex", 1);
        Integer increment = getIntegerValue(step, "increment", 1);
        String xpath = (String) step.get("xpath");
        String value = (String) step.get("value");

        return "            {\n" +
                "                System.out.println(\"🔄 开始遍历输入操作，范围: " + startIndex + " - " + endIndex + "，增量: " + increment + "\");\n" +
                "                for (int i = " + startIndex + "; i <= " + endIndex + "; i += " + increment + ") {\n" +
                "                    context.put(\"i\", i);\n" +
                "                    context.put(\"index\", i);\n" +
                "                    context.put(\"current\", i);\n" +
                "                    String resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "                    String resolvedValue = resolveValueWithExpression(\"" + value + "\", context);\n" +
                "                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));\n" +
                "                    try {\n" +
                "                        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(resolvedXpath)));\n" +
                "                        element.clear();\n" +
                "                        element.sendKeys(resolvedValue);\n" +
                "                        System.out.println(\"✅ 遍历输入成功: 索引: \" + i + \", 值: '\" + resolvedValue + \"', XPath: \" + resolvedXpath);\n" +
                "                        safeWait(500);\n" +
                "                    } catch (Exception e) {\n" +
                "                        System.out.println(\"⚠️ 遍历输入失败，索引: \" + i + \", XPath: \" + resolvedXpath);\n" +
                "                    }\n" +
                "                }\n" +
                "                context.remove(\"i\");\n" +
                "                context.remove(\"index\");\n" +
                "                context.remove(\"current\");\n" +
                "            }\n";
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

        return "            {\n" +
                "                System.out.println(\"🔄 开始遍历获取文本操作，范围: " + startIndex + " - " + endIndex + "，增量: " + increment + "\");\n" +
                "                List<String> textList = new ArrayList<>();\n" +
                "                for (int i = " + startIndex + "; i <= " + endIndex + "; i += " + increment + ") {\n" +
                "                    context.put(\"i\", i);\n" +
                "                    context.put(\"index\", i);\n" +
                "                    context.put(\"current\", i);\n" +
                "                    String resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));\n" +
                "                    try {\n" +
                "                        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(resolvedXpath)));\n" +
                "                        String text = element.getText().trim();\n" +
                "                        textList.add(text);\n" +
                "                        System.out.println(\"📖 获取文本 [\" + i + \"]: '\" + text + \"'\");\n" +
                "                        safeWait(500);\n" +
                "                    } catch (Exception e) {\n" +
                "                        System.out.println(\"⚠️ 获取文本失败，索引: \" + i);\n" +
                "                        textList.add(\"\");\n" +
                "                    }\n" +
                "                }\n" +
                "                context.remove(\"i\");\n" +
                "                context.remove(\"index\");\n" +
                "                context.remove(\"current\");\n" +
                "                \n" +
                "                // 写入文件\n" +
                "                try {\n" +
                "                    Files.write(Paths.get(\"" + filePath + "\"), textList, StandardCharsets.UTF_8);\n" +
                "                    System.out.println(\"💾 成功将 \" + textList.size() + \" 条文本写入文件: " + filePath + "\");\n" +
                "                } catch (Exception e) {\n" +
                "                    System.out.println(\"❌ 写入文件失败: " + filePath + "\");\n" +
                "                }\n" +
                "            }\n";
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
        loopCode.append("            {\n");
        loopCode.append("                System.out.println(\"🔄 开始循环任务，迭代次数: ").append(iterations).append("，增量: ").append(increment).append("\");\n");
        loopCode.append("                for (int ").append(loopVar).append(" = 0; ").append(loopVar).append(" < ").append(iterations).append("; ").append(loopVar).append(" += ").append(increment).append(") {\n");
        loopCode.append("                    context.put(\"").append(loopVar).append("\", ").append(loopVar).append(");\n");
        loopCode.append("                    context.put(\"i\", ").append(loopVar).append(");\n");
        loopCode.append("                    context.put(\"index\", ").append(loopVar).append(");\n");
        loopCode.append("                    System.out.println(\"🔄 循环任务迭代: \" + (").append(loopVar).append(" + 1) + \"/").append(iterations).append("\");\n");

        // 生成子步骤代码
        if (subSteps != null) {
            for (int i = 0; i < subSteps.size(); i++) {
                Map<String, Object> subStep = subSteps.get(i);
                String subStepCode = generateStepCode(subStep, i + 1, false)
                        .replace("            ", "                    ");
                loopCode.append(subStepCode).append("\n");
            }
        }

        loopCode.append("                }\n");
        loopCode.append("                context.remove(\"").append(loopVar).append("\");\n");
        loopCode.append("                context.remove(\"i\");\n");
        loopCode.append("                context.remove(\"index\");\n");
        loopCode.append("            }\n");
        return loopCode.toString();
    }

    private String generateDynamicLoop(Map<String, Object> step, int stepNumber) {
        Integer iterations = getIntegerValue(step, "iterations", 1);
        Integer increment = getIntegerValue(step, "increment", 1);
        String loopVar = (String) step.get("value");
        if (loopVar == null) {
            loopVar = "dynamic_index";
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> subSteps = (List<Map<String, Object>>) step.get("subSteps");

        StringBuilder loopCode = new StringBuilder();
        loopCode.append("            {\n");
        loopCode.append("                System.out.println(\"🔄 开始动态循环，迭代次数: ").append(iterations).append("，增量: ").append(increment).append("\");\n");
        loopCode.append("                for (int ").append(loopVar).append(" = 0; ").append(loopVar).append(" < ").append(iterations).append("; ").append(loopVar).append(" += ").append(increment).append(") {\n");
        loopCode.append("                    context.put(\"").append(loopVar).append("\", ").append(loopVar).append(");\n");
        loopCode.append("                    context.put(\"i\", ").append(loopVar).append(");\n");
        loopCode.append("                    context.put(\"index\", ").append(loopVar).append(");\n");
        loopCode.append("                    System.out.println(\"🔄 动态循环迭代: \" + (").append(loopVar).append(" + 1) + \"/").append(iterations).append("\");\n");

        // 生成子步骤代码
        if (subSteps != null) {
            for (int i = 0; i < subSteps.size(); i++) {
                Map<String, Object> subStep = subSteps.get(i);
                String subStepCode = generateStepCode(subStep, i + 1, false)
                        .replace("            ", "                    ");
                loopCode.append(subStepCode).append("\n");
            }
        }

        loopCode.append("                    safeWait(1000);\n");
        loopCode.append("                }\n");
        loopCode.append("                context.remove(\"").append(loopVar).append("\");\n");
        loopCode.append("                context.remove(\"i\");\n");
        loopCode.append("                context.remove(\"index\");\n");
        loopCode.append("            }\n");
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
        return "            System.out.println(\"⏳ 等待 \" + " + waitTime + " + \" 毫秒\");\n" +
                "            safeWait(" + waitTime + "L);\n";
    }

    private String generateNavigate(Map<String, Object> step) {
        String url = (String) step.get("value");
        return "            driver.get(\"" + url + "\");\n" +
                "            System.out.println(\"🌐 导航到: " + url + "\");\n";
    }

    private String generateSwitchIframe(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        return "            {\n" +
                "                String resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "                if (\"default\".equalsIgnoreCase(resolvedXpath)) {\n" +
                "                    driver.switchTo().defaultContent();\n" +
                "                    System.out.println(\"✅ 已切换回默认内容\");\n" +
                "                } else if (\"parent\".equalsIgnoreCase(resolvedXpath)) {\n" +
                "                    driver.switchTo().parentFrame();\n" +
                "                    System.out.println(\"✅ 已切换回父级iframe\");\n" +
                "                } else {\n" +
                "                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));\n" +
                "                    WebElement iframeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(resolvedXpath)));\n" +
                "                    driver.switchTo().frame(iframeElement);\n" +
                "                    System.out.println(\"✅ 已切换到iframe: \" + resolvedXpath);\n" +
                "                }\n" +
                "            }\n";
    }

    private String generateGetCurrentUrl(Map<String, Object> step) {
        String key = (String) step.get("value");
        if (key == null) {
            key = "current_url";
        }
        return "            {\n" +
                "                String currentUrl = driver.getCurrentUrl();\n" +
                "                context.put(\"" + key + "\", currentUrl);\n" +
                "                System.out.println(\"🌐 获取当前URL: \" + currentUrl);\n" +
                "                System.out.println(\"💾 已保存到上下文: " + key + " = \" + currentUrl);\n" +
                "            }\n";
    }

    private String generatePressKeys(Map<String, Object> step) {
        String keys = (String) step.get("value");
        return "            {\n" +
                "                String resolvedValue = resolveValueWithExpression(\"" + keys + "\", context);\n" +
                "                Actions actions = new Actions(driver);\n" +
                generateKeyActions("resolvedValue") +
                "                actions.perform();\n" +
                "                System.out.println(\"⌨️ 模拟按键: \" + resolvedValue);\n" +
                "            }\n";
    }

    private String generateKeyboardInput(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        String value = (String) step.get("value");
        return "            {\n" +
                "                String resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "                String resolvedValue = resolveValueWithExpression(\"" + value + "\", context);\n" +
                "                Actions actions = new Actions(driver);\n" +
                "                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));\n" +
                "                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(resolvedXpath)));\n" +
                "                element.click();\n" +
                "                actions.sendKeys(parseKeySequence(resolvedValue)).perform();\n" +
                "                System.out.println(\"⌨️ 键盘输入: '\" + resolvedValue + \"' 到元素: \" + resolvedXpath);\n" +
                "            }\n";
    }

    private String generateGoBack(Map<String, Object> step) {
        return "            driver.navigate().back();\n" +
                "            System.out.println(\"↩️ 返回上一页\");\n";
    }

    private String generateHandleAlert(Map<String, Object> step) {
        Boolean acceptAlert = (Boolean) step.get("acceptAlert");
        String alertText = (String) step.get("alertText");

        StringBuilder alertCode = new StringBuilder();
        alertCode.append("            try {\n");
        alertCode.append("                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));\n");
        alertCode.append("                Alert alert = wait.until(ExpectedConditions.alertIsPresent());\n");

        if (alertText != null && !alertText.isEmpty()) {
            alertCode.append("                alert.sendKeys(\"").append(alertText).append("\");\n");
            alertCode.append("                System.out.println(\"⌨️ 在弹窗中输入文本: ").append(alertText).append("\");\n");
        }

        if (acceptAlert != null) {
            if (acceptAlert) {
                alertCode.append("                alert.accept();\n");
                alertCode.append("                System.out.println(\"✅ 接受弹窗\");\n");
            } else {
                alertCode.append("                alert.dismiss();\n");
                alertCode.append("                System.out.println(\"❌ 取消弹窗\");\n");
            }
        }

        alertCode.append("            } catch (Exception e) {\n");
        alertCode.append("                System.out.println(\"⚠️ 未检测到弹窗\");\n");
        alertCode.append("            }\n");

        return alertCode.toString();
    }

    private String generateCloseTab(Map<String, Object> step) {
        return "            {\n" +
                "                String currentHandle = driver.getWindowHandle();\n" +
                "                Set<String> handles = driver.getWindowHandles();\n" +
                "                if (handles.size() > 1) {\n" +
                "                    driver.close();\n" +
                "                    handles = driver.getWindowHandles();\n" +
                "                    if (!handles.isEmpty()) {\n" +
                "                        String newHandle = handles.iterator().next();\n" +
                "                        driver.switchTo().window(newHandle);\n" +
                "                        System.out.println(\"✅ 关闭标签页，切换到新标签页\");\n" +
                "                        System.out.println(\"🌐 当前URL: \" + driver.getCurrentUrl());\n" +
                "                    }\n" +
                "                } else {\n" +
                "                    System.out.println(\"⚠️ 只有一个标签页，无法关闭\");\n" +
                "                }\n" +
                "            }\n";
    }

    private String generateImportCookie(Map<String, Object> step) {
        String filePath = (String) step.get("filePath");
        return "            {\n" +
                "                try {\n" +
                "                    System.out.println(\"🍪 开始从文件导入Cookie: " + filePath + "\");\n" +
                "                    String cookieJson = new String(Files.readAllBytes(Paths.get(\"" + filePath + "\")), StandardCharsets.UTF_8);\n" +
                "                    ObjectMapper objectMapper = new ObjectMapper();\n" +
                "                    List<Map<String, Object>> cookies = objectMapper.readValue(cookieJson, new TypeReference<List<Map<String, Object>>>(){});\n" +
                "                    \n" +
                "                    int importedCount = 0;\n" +
                "                    for (Map<String, Object> cookieMap : cookies) {\n" +
                "                        try {\n" +
                "                            Cookie.Builder cookieBuilder = new Cookie.Builder(\n" +
                "                                cookieMap.get(\"name\").toString(),\n" +
                "                                cookieMap.get(\"value\").toString()\n" +
                "                            );\n" +
                "                            cookieBuilder.domain(cookieMap.get(\"domain\").toString());\n" +
                "                            if (cookieMap.containsKey(\"path\")) {\n" +
                "                                cookieBuilder.path(cookieMap.get(\"path\").toString());\n" +
                "                            } else {\n" +
                "                                cookieBuilder.path(\"/\");\n" +
                "                            }\n" +
                "                            driver.manage().addCookie(cookieBuilder.build());\n" +
                "                            importedCount++;\n" +
                "                            System.out.println(\"✅ 导入Cookie: \" + cookieMap.get(\"name\"));\n" +
                "                        } catch (Exception e) {\n" +
                "                            System.out.println(\"⚠️ 导入单个Cookie失败: \" + cookieMap.get(\"name\"));\n" +
                "                        }\n" +
                "                    }\n" +
                "                    System.out.println(\"✅ 成功导入 \" + importedCount + \" 个Cookie\");\n" +
                "                    \n" +
                "                    // 刷新页面使Cookie生效\n" +
                "                    driver.navigate().refresh();\n" +
                "                    System.out.println(\"🔄 已刷新页面使Cookie生效\");\n" +
                "                    safeWait(2000);\n" +
                "                } catch (Exception e) {\n" +
                "                    System.out.println(\"❌ 导入Cookie失败: \" + e.getMessage());\n" +
                "                }\n" +
                "            }\n";
    }

    private String generateKeyActions(String keyVariable) {
        return "                String[] keySequence = " + keyVariable + ".split(\"\\\\+\");\n" +
                "                for (String key : keySequence) {\n" +
                "                    key = key.trim().toUpperCase();\n" +
                "                    switch (key) {\n" +
                "                        case \"CTRL\":\n" +
                "                            actions.keyDown(Keys.CONTROL);\n" +
                "                            break;\n" +
                "                        case \"SHIFT\":\n" +
                "                            actions.keyDown(Keys.SHIFT);\n" +
                "                            break;\n" +
                "                        case \"ALT\":\n" +
                "                            actions.keyDown(Keys.ALT);\n" +
                "                            break;\n" +
                "                        case \"ENTER\":\n" +
                "                            actions.sendKeys(Keys.ENTER);\n" +
                "                            break;\n" +
                "                        case \"TAB\":\n" +
                "                            actions.sendKeys(Keys.TAB);\n" +
                "                            break;\n" +
                "                        case \"ESC\":\n" +
                "                            actions.sendKeys(Keys.ESCAPE);\n" +
                "                            break;\n" +
                "                        case \"BACKSPACE\":\n" +
                "                            actions.sendKeys(Keys.BACK_SPACE);\n" +
                "                            break;\n" +
                "                        case \"DELETE\":\n" +
                "                            actions.sendKeys(Keys.DELETE);\n" +
                "                            break;\n" +
                "                        case \"HOME\":\n" +
                "                            actions.sendKeys(Keys.HOME);\n" +
                "                            break;\n" +
                "                        case \"END\":\n" +
                "                            actions.sendKeys(Keys.END);\n" +
                "                            break;\n" +
                "                        case \"PAGEUP\":\n" +
                "                            actions.sendKeys(Keys.PAGE_UP);\n" +
                "                            break;\n" +
                "                        case \"PAGEDOWN\":\n" +
                "                            actions.sendKeys(Keys.PAGE_DOWN);\n" +
                "                            break;\n" +
                "                        case \"ARROW_UP\":\n" +
                "                            actions.sendKeys(Keys.ARROW_UP);\n" +
                "                            break;\n" +
                "                        case \"ARROW_DOWN\":\n" +
                "                            actions.sendKeys(Keys.ARROW_DOWN);\n" +
                "                            break;\n" +
                "                        case \"ARROW_LEFT\":\n" +
                "                            actions.sendKeys(Keys.ARROW_LEFT);\n" +
                "                            break;\n" +
                "                        case \"ARROW_RIGHT\":\n" +
                "                            actions.sendKeys(Keys.ARROW_RIGHT);\n" +
                "                            break;\n" +
                "                        default:\n" +
                "                            if (key.matches(\"F[1-9]|F1[0-2]\")) {\n" +
                "                                actions.sendKeys(Keys.valueOf(key));\n" +
                "                            } else if (key.length() == 1 && Character.isLetter(key.charAt(0))) {\n" +
                "                                actions.sendKeys(key);\n" +
                "                            } else {\n" +
                "                                System.out.println(\"⚠️ 不支持的按键: \" + key);\n" +
                "                            }\n" +
                "                    }\n" +
                "                }\n" +
                "                actions.keyUp(Keys.CONTROL).keyUp(Keys.SHIFT).keyUp(Keys.ALT);\n";
    }

    private String generateHelperMethods() {
        return "    // 辅助方法\n" +
                "    private static void safeWait(long milliseconds) {\n" +
                "        try {\n" +
                "            Thread.sleep(milliseconds);\n" +
                "        } catch (InterruptedException e) {\n" +
                "            Thread.currentThread().interrupt();\n" +
                "        }\n" +
                "    }\n\n" +
                "    // 表达式解析方法\n" +
                "    private static String resolveXpathWithExpression(String xpath, Map<String, Object> context) {\n" +
                "        if (xpath == null) return null;\n" +
                "        String result = xpath;\n" +
                "        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(\"\\\\{([^}]+)\\\\}\");\n" +
                "        java.util.regex.Matcher matcher = pattern.matcher(xpath);\n" +
                "        \n" +
                "        while (matcher.find()) {\n" +
                "            String fullMatch = matcher.group(0);\n" +
                "            String expression = matcher.group(1);\n" +
                "            try {\n" +
                "                int value = parseExpression(fullMatch, context);\n" +
                "                result = result.replace(fullMatch, String.valueOf(value));\n" +
                "            } catch (Exception e) {\n" +
                "                // 解析失败，保持原样\n" +
                "            }\n" +
                "        }\n" +
                "        return result;\n" +
                "    }\n\n" +
                "    private static String resolveValueWithExpression(String value, Map<String, Object> context) {\n" +
                "        if (value == null) return null;\n" +
                "        String result = value;\n" +
                "        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(\"\\\\{([^}]+)\\\\}\");\n" +
                "        java.util.regex.Matcher matcher = pattern.matcher(value);\n" +
                "        \n" +
                "        while (matcher.find()) {\n" +
                "            String fullMatch = matcher.group(0);\n" +
                "            String expression = matcher.group(1);\n" +
                "            try {\n" +
                "                int exprValue = parseExpression(fullMatch, context);\n" +
                "                result = result.replace(fullMatch, String.valueOf(exprValue));\n" +
                "            } catch (Exception e) {\n" +
                "                // 解析失败，保持原样\n" +
                "            }\n" +
                "        }\n" +
                "        return result;\n" +
                "    }\n\n" +
                "    private static int parseExpression(String expression, Map<String, Object> context) {\n" +
                "        String expr = expression.replace(\"{\", \"\").replace(\"}\", \"\").trim();\n" +
                "        \n" +
                "        if (expr.matches(\"\\\\d+\")) {\n" +
                "            return Integer.parseInt(expr);\n" +
                "        }\n" +
                "        \n" +
                "        for (Map.Entry<String, Object> entry : context.entrySet()) {\n" +
                "            String varName = entry.getKey();\n" +
                "            if (expr.startsWith(varName)) {\n" +
                "                int baseValue = Integer.parseInt(entry.getValue().toString());\n" +
                "                String operatorPart = expr.substring(varName.length()).trim();\n" +
                "                \n" +
                "                if (operatorPart.isEmpty()) {\n" +
                "                    return baseValue;\n" +
                "                }\n" +
                "                \n" +
                "                if (operatorPart.matches(\"[+\\\\-*/]\\\\s*\\\\d+\")) {\n" +
                "                    char operator = operatorPart.charAt(0);\n" +
                "                    int number = Integer.parseInt(operatorPart.substring(1).trim());\n" +
                "                    \n" +
                "                    switch (operator) {\n" +
                "                        case '+':\n" +
                "                            return baseValue + number;\n" +
                "                        case '-':\n" +
                "                            return baseValue - number;\n" +
                "                        case '*':\n" +
                "                            return baseValue * number;\n" +
                "                        case '/':\n" +
                "                            return baseValue / number;\n" +
                "                        default:\n" +
                "                            return baseValue;\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        return 1;\n" +
                "    }\n\n" +
                "    private static CharSequence[] parseKeySequence(String input) {\n" +
                "        List<CharSequence> sequence = new ArrayList<>();\n" +
                "        StringBuilder currentText = new StringBuilder();\n" +
                "        \n" +
                "        for (int i = 0; i < input.length(); i++) {\n" +
                "            char c = input.charAt(i);\n" +
                "            \n" +
                "            if (c == '{' && i + 1 < input.length()) {\n" +
                "                int endIndex = input.indexOf('}', i);\n" +
                "                if (endIndex != -1) {\n" +
                "                    String specialKey = input.substring(i + 1, endIndex).toUpperCase();\n" +
                "                    if (currentText.length() > 0) {\n" +
                "                        sequence.add(currentText.toString());\n" +
                "                        currentText.setLength(0);\n" +
                "                    }\n" +
                "                    \n" +
                "                    switch (specialKey) {\n" +
                "                        case \"ENTER\":\n" +
                "                            sequence.add(Keys.ENTER);\n" +
                "                            break;\n" +
                "                        case \"TAB\":\n" +
                "                            sequence.add(Keys.TAB);\n" +
                "                            break;\n" +
                "                        case \"ESC\":\n" +
                "                        case \"ESCAPE\":\n" +
                "                            sequence.add(Keys.ESCAPE);\n" +
                "                            break;\n" +
                "                        case \"BACKSPACE\":\n" +
                "                            sequence.add(Keys.BACK_SPACE);\n" +
                "                            break;\n" +
                "                        case \"DELETE\":\n" +
                "                            sequence.add(Keys.DELETE);\n" +
                "                            break;\n" +
                "                        case \"HOME\":\n" +
                "                            sequence.add(Keys.HOME);\n" +
                "                            break;\n" +
                "                        case \"END\":\n" +
                "                            sequence.add(Keys.END);\n" +
                "                            break;\n" +
                "                        case \"PAGEUP\":\n" +
                "                            sequence.add(Keys.PAGE_UP);\n" +
                "                            break;\n" +
                "                        case \"PAGEDOWN\":\n" +
                "                            sequence.add(Keys.PAGE_DOWN);\n" +
                "                            break;\n" +
                "                        case \"UP\":\n" +
                "                        case \"ARROW_UP\":\n" +
                "                            sequence.add(Keys.ARROW_UP);\n" +
                "                            break;\n" +
                "                        case \"DOWN\":\n" +
                "                        case \"ARROW_DOWN\":\n" +
                "                            sequence.add(Keys.ARROW_DOWN);\n" +
                "                            break;\n" +
                "                        case \"LEFT\":\n" +
                "                        case \"ARROW_LEFT\":\n" +
                "                            sequence.add(Keys.ARROW_LEFT);\n" +
                "                            break;\n" +
                "                        case \"RIGHT\":\n" +
                "                        case \"ARROW_RIGHT\":\n" +
                "                            sequence.add(Keys.ARROW_RIGHT);\n" +
                "                            break;\n" +
                "                        case \"CTRL\":\n" +
                "                            sequence.add(Keys.CONTROL);\n" +
                "                            break;\n" +
                "                        case \"SHIFT\":\n" +
                "                            sequence.add(Keys.SHIFT);\n" +
                "                            break;\n" +
                "                        case \"ALT\":\n" +
                "                            sequence.add(Keys.ALT);\n" +
                "                            break;\n" +
                "                        default:\n" +
                "                            if (specialKey.matches(\"F[1-9]|F1[0-2]\")) {\n" +
                "                                sequence.add(Keys.valueOf(specialKey));\n" +
                "                            } else {\n" +
                "                                currentText.append(\"{\" + specialKey + \"}\");\n" +
                "                            }\n" +
                "                    }\n" +
                "                    i = endIndex;\n" +
                "                } else {\n" +
                "                    currentText.append(c);\n" +
                "                }\n" +
                "            } else {\n" +
                "                currentText.append(c);\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        if (currentText.length() > 0) {\n" +
                "            sequence.add(currentText.toString());\n" +
                "        }\n" +
                "        \n" +
                "        return sequence.toArray(new CharSequence[0]);\n" +
                "    }\n\n";
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
        return "java";
    }

    @Override
    public String getFileExtension() {
        return ".java";
    }
}