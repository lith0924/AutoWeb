package org.example.auto_web.service.strategy;

import org.example.auto_web.service.CodeGenerationStrategy;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class CppCodeGenerationStrategy implements CodeGenerationStrategy {

    @Override
    public String generateCode(List<Map<String, Object>> steps, String initialUrl, String className, Boolean includeComments) {
        StringBuilder code = new StringBuilder();

        // 头文件
        code.append("#include <webdriverxx/webdriverxx.h>\n");
        code.append("#include <webdriverxx/wait.h>\n");
        code.append("#include <webdriverxx/keys.h>\n");
        code.append("#include <iostream>\n");
        code.append("#include <thread>\n");
        code.append("#include <chrono>\n");
        code.append("#include <fstream>\n");
        code.append("#include <vector>\n");
        code.append("#include <string>\n");
        code.append("#include <algorithm>\n");
        code.append("#include <map>\n");
        code.append("#include <regex>\n");
        code.append("#include <sstream>\n\n");

        // 注释
        if (includeComments) {
            code.append("/*\n");
            code.append(" * 自动生成的Selenium测试程序\n");
            code.append(" * 生成时间: ").append(new Date()).append("\n");
            code.append(" * 步骤数量: ").append(steps.size()).append("\n");
            code.append(" */\n\n");
        }

        // 辅助函数声明
        code.append("// 辅助函数声明\n");
        code.append("void safeWait(int milliseconds);\n");
        code.append("std::string resolveXpathWithExpression(const std::string& xpath, const std::map<std::string, int>& context);\n");
        code.append("std::string resolveValueWithExpression(const std::string& value, const std::map<std::string, int>& context);\n");
        code.append("int parseExpression(const std::string& expression, const std::map<std::string, int>& context);\n");
        code.append("std::vector<std::string> parseKeySequence(const std::string& input);\n\n");

        code.append("int main() {\n");
        code.append("    using namespace webdriverxx;\n");
        code.append("    WebDriver driver = Start(Chrome());\n");
        code.append("    std::map<std::string, int> context;\n");
        code.append("    \n");
        code.append("    try {\n");

        // 初始导航
        if (initialUrl != null && !initialUrl.isEmpty()) {
            code.append("        // 初始导航\n");
            code.append("        driver.Navigate(\"").append(initialUrl).append("\");\n");
            code.append("        std::cout << \"✅ 初始导航到: ").append(initialUrl).append("\" << std::endl;\n");
            code.append("        safeWait(2000);\n\n");
        }

        // 生成步骤代码
        for (int i = 0; i < steps.size(); i++) {
            Map<String, Object> step = steps.get(i);
            code.append(generateStepCode(step, i + 1, includeComments));
            code.append("\n");
        }

        code.append("        std::cout << \"🎉 所有操作执行完成\" << std::endl;\n");
        code.append("        \n");
        code.append("    } catch (const std::exception& e) {\n");
        code.append("        std::cerr << \"❌ 执行失败: \" << e.what() << std::endl;\n");
        code.append("    }\n");
        code.append("    \n");
        code.append("    driver.Quit();\n");
        code.append("    std::cout << \"🔚 浏览器已关闭\" << std::endl;\n");
        code.append("    return 0;\n");
        code.append("}\n\n");

        // 辅助函数实现
        code.append(generateHelperMethods());

        return code.toString();
    }

    private String generateStepCode(Map<String, Object> step, int stepNumber, Boolean includeComments) {
        String type = (String) step.get("type");
        String remark = (String) step.get("remark");
        StringBuilder stepCode = new StringBuilder();

        // 步骤注释
        if (includeComments) {
            stepCode.append("        // 步骤 ").append(stepNumber);
            if (remark != null && !remark.isEmpty()) {
                stepCode.append(": ").append(remark);
            }
            stepCode.append("\n");
        }

        // 操作前等待
        Long waitBeforeMs = getLongValue(step, "waitBeforeMs");
        if (waitBeforeMs != null && waitBeforeMs > 0) {
            stepCode.append("        safeWait(").append(waitBeforeMs).append(");\n");
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
                stepCode.append("        // 不支持的操作类型: ").append(type).append("\n");
        }

        // 操作后等待
        Long waitAfterMs = getLongValue(step, "waitAfterMs");
        if (waitAfterMs != null && waitAfterMs > 0) {
            stepCode.append("        safeWait(").append(waitAfterMs).append(");\n");
        }

        return stepCode.toString();
    }

    private String generateClick(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        return "        {\n" +
                "            std::string resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "            auto element = driver.FindElement(webdriverxx::ByXPath(resolvedXpath));\n" +
                "            element.Click();\n" +
                "            std::cout << \"✅ 点击元素: \" << resolvedXpath << std::endl;\n" +
                "        }\n";
    }

    private String generateInput(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        String value = (String) step.get("value");
        return "        {\n" +
                "            std::string resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "            std::string resolvedValue = resolveValueWithExpression(\"" + value + "\", context);\n" +
                "            auto element = driver.FindElement(webdriverxx::ByXPath(resolvedXpath));\n" +
                "            element.Clear();\n" +
                "            element.SendKeys(resolvedValue);\n" +
                "            std::cout << \"✅ 输入内容: '\" << resolvedValue << \"' 到元素: \" << resolvedXpath << std::endl;\n" +
                "        }\n";
    }

    private String generateGetText(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        return "        {\n" +
                "            std::string resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "            auto element = driver.FindElement(webdriverxx::ByXPath(resolvedXpath));\n" +
                "            std::string text = element.GetText();\n" +
                "            std::cout << \"📖 获取文本: '\" << text << \"' 从元素: \" << resolvedXpath << std::endl;\n" +
                "        }\n";
    }

    private String generateLoopClick(Map<String, Object> step) {
        Integer startIndex = getIntegerValue(step, "startIndex", 1);
        Integer endIndex = getIntegerValue(step, "endIndex", 1);
        Integer increment = getIntegerValue(step, "increment", 1);
        String xpath = (String) step.get("xpath");

        return "        {\n" +
                "            std::cout << \"🔄 开始遍历点击操作，范围: " + startIndex + " - " + endIndex + "，增量: " + increment + "\" << std::endl;\n" +
                "            for (int i = " + startIndex + "; i <= " + endIndex + "; i += " + increment + ") {\n" +
                "                context[\"i\"] = i;\n" +
                "                context[\"index\"] = i;\n" +
                "                context[\"current\"] = i;\n" +
                "                std::string resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "                try {\n" +
                "                    auto element = driver.FindElement(webdriverxx::ByXPath(resolvedXpath));\n" +
                "                    element.Click();\n" +
                "                    std::cout << \"✅ 遍历点击成功: 索引: \" << i << \", XPath: \" << resolvedXpath << std::endl;\n" +
                "                    safeWait(500);\n" +
                "                } catch (const std::exception& e) {\n" +
                "                    std::cout << \"⚠️ 遍历点击失败，索引: \" << i << \", XPath: \" << resolvedXpath << std::endl;\n" +
                "                }\n" +
                "            }\n" +
                "            context.erase(\"i\");\n" +
                "            context.erase(\"index\");\n" +
                "            context.erase(\"current\");\n" +
                "        }\n";
    }

    private String generateLoopInput(Map<String, Object> step) {
        Integer startIndex = getIntegerValue(step, "startIndex", 1);
        Integer endIndex = getIntegerValue(step, "endIndex", 1);
        Integer increment = getIntegerValue(step, "increment", 1);
        String xpath = (String) step.get("xpath");
        String value = (String) step.get("value");

        return "        {\n" +
                "            std::cout << \"🔄 开始遍历输入操作，范围: " + startIndex + " - " + endIndex + "，增量: " + increment + "\" << std::endl;\n" +
                "            for (int i = " + startIndex + "; i <= " + endIndex + "; i += " + increment + ") {\n" +
                "                context[\"i\"] = i;\n" +
                "                context[\"index\"] = i;\n" +
                "                context[\"current\"] = i;\n" +
                "                std::string resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "                std::string resolvedValue = resolveValueWithExpression(\"" + value + "\", context);\n" +
                "                try {\n" +
                "                    auto element = driver.FindElement(webdriverxx::ByXPath(resolvedXpath));\n" +
                "                    element.Clear();\n" +
                "                    element.SendKeys(resolvedValue);\n" +
                "                    std::cout << \"✅ 遍历输入成功: 索引: \" << i << \", 值: '\" << resolvedValue << \"', XPath: \" << resolvedXpath << std::endl;\n" +
                "                    safeWait(500);\n" +
                "                } catch (const std::exception& e) {\n" +
                "                    std::cout << \"⚠️ 遍历输入失败，索引: \" << i << \", XPath: \" << resolvedXpath << std::endl;\n" +
                "                }\n" +
                "            }\n" +
                "            context.erase(\"i\");\n" +
                "            context.erase(\"index\");\n" +
                "            context.erase(\"current\");\n" +
                "        }\n";
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

        return "        {\n" +
                "            std::cout << \"🔄 开始遍历获取文本操作，范围: " + startIndex + " - " + endIndex + "，增量: " + increment + "\" << std::endl;\n" +
                "            std::vector<std::string> textList;\n" +
                "            for (int i = " + startIndex + "; i <= " + endIndex + "; i += " + increment + ") {\n" +
                "                context[\"i\"] = i;\n" +
                "                context[\"index\"] = i;\n" +
                "                context[\"current\"] = i;\n" +
                "                std::string resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "                try {\n" +
                "                    auto element = driver.FindElement(webdriverxx::ByXPath(resolvedXpath));\n" +
                "                    std::string text = element.GetText();\n" +
                "                    // 去除首尾空格\n" +
                "                    text.erase(text.begin(), std::find_if(text.begin(), text.end(), [](unsigned char ch) {\n" +
                "                        return !std::isspace(ch);\n" +
                "                    }));\n" +
                "                    text.erase(std::find_if(text.rbegin(), text.rend(), [](unsigned char ch) {\n" +
                "                        return !std::isspace(ch);\n" +
                "                    }).base(), text.end());\n" +
                "                    textList.push_back(text);\n" +
                "                    std::cout << \"📖 获取文本 [\" << i << \"]: '\" << text << \"'\" << std::endl;\n" +
                "                    safeWait(500);\n" +
                "                } catch (const std::exception& e) {\n" +
                "                    std::cout << \"⚠️ 获取文本失败，索引: \" << i << std::endl;\n" +
                "                    textList.push_back(\"\");\n" +
                "                }\n" +
                "            }\n" +
                "            context.erase(\"i\");\n" +
                "            context.erase(\"index\");\n" +
                "            context.erase(\"current\");\n" +
                "            // 写入文件\n" +
                "            try {\n" +
                "                std::ofstream file(\"" + filePath + "\");\n" +
                "                for (const auto& text : textList) {\n" +
                "                    file << text << \"\\n\";\n" +
                "                }\n" +
                "                file.close();\n" +
                "                std::cout << \"💾 成功将 \" << textList.size() << \" 条文本写入文件: " + filePath + "\" << std::endl;\n" +
                "            } catch (const std::exception& e) {\n" +
                "                std::cout << \"❌ 写入文件失败: " + filePath + "\" << std::endl;\n" +
                "            }\n" +
                "        }\n";
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
        loopCode.append("        {\n");
        loopCode.append("            std::cout << \"🔄 开始循环任务，迭代次数: ").append(iterations).append("，增量: ").append(increment).append("\" << std::endl;\n");
        loopCode.append("            for (int ").append(loopVar).append(" = 0; ").append(loopVar).append(" < ").append(iterations).append("; ").append(loopVar).append(" += ").append(increment).append(") {\n");
        loopCode.append("                context[\"").append(loopVar).append("\"] = ").append(loopVar).append(";\n");
        loopCode.append("                context[\"i\"] = ").append(loopVar).append(";\n");
        loopCode.append("                context[\"index\"] = ").append(loopVar).append(";\n");
        loopCode.append("                std::cout << \"🔄 循环任务迭代: \" << (").append(loopVar).append(" + 1) << \"/").append(iterations).append("\" << std::endl;\n");

        // 生成子步骤代码
        if (subSteps != null) {
            for (int i = 0; i < subSteps.size(); i++) {
                Map<String, Object> subStep = subSteps.get(i);
                String subStepCode = generateStepCode(subStep, i + 1, false)
                        .replace("        ", "                ");
                loopCode.append(subStepCode).append("\n");
            }
        }

        loopCode.append("            }\n");
        loopCode.append("            context.erase(\"").append(loopVar).append("\");\n");
        loopCode.append("            context.erase(\"i\");\n");
        loopCode.append("            context.erase(\"index\");\n");
        loopCode.append("        }\n");
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
        loopCode.append("        {\n");
        loopCode.append("            std::cout << \"🔄 开始动态循环，迭代次数: ").append(iterations).append("，增量: ").append(increment).append("\" << std::endl;\n");
        loopCode.append("            for (int ").append(loopVar).append(" = 0; ").append(loopVar).append(" < ").append(iterations).append("; ").append(loopVar).append(" += ").append(increment).append(") {\n");
        loopCode.append("                context[\"").append(loopVar).append("\"] = ").append(loopVar).append(";\n");
        loopCode.append("                context[\"i\"] = ").append(loopVar).append(";\n");
        loopCode.append("                context[\"index\"] = ").append(loopVar).append(";\n");
        loopCode.append("                std::cout << \"🔄 动态循环迭代: \" << (").append(loopVar).append(" + 1) << \"/").append(iterations).append("\" << std::endl;\n");

        // 生成子步骤代码
        if (subSteps != null) {
            for (int i = 0; i < subSteps.size(); i++) {
                Map<String, Object> subStep = subSteps.get(i);
                String subStepCode = generateStepCode(subStep, i + 1, false)
                        .replace("        ", "                ");
                loopCode.append(subStepCode).append("\n");
            }
        }

        loopCode.append("                safeWait(1000);\n");
        loopCode.append("            }\n");
        loopCode.append("            context.erase(\"").append(loopVar).append("\");\n");
        loopCode.append("            context.erase(\"i\");\n");
        loopCode.append("            context.erase(\"index\");\n");
        loopCode.append("        }\n");
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
        return "        std::cout << \"⏳ 等待 " + waitTime + " 毫秒\" << std::endl;\n" +
                "        safeWait(" + waitTime + ");\n";
    }

    private String generateNavigate(Map<String, Object> step) {
        String url = (String) step.get("value");
        return "        driver.Navigate(\"" + url + "\");\n" +
                "        std::cout << \"🌐 导航到: " + url + "\" << std::endl;\n";
    }

    private String generateSwitchIframe(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        return "        {\n" +
                "            std::string resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "            if (resolvedXpath == \"default\" || resolvedXpath == \"DEFAULT\") {\n" +
                "                driver.SwitchTo().DefaultContent();\n" +
                "                std::cout << \"✅ 已切换回默认内容\" << std::endl;\n" +
                "            } else if (resolvedXpath == \"parent\" || resolvedXpath == \"PARENT\") {\n" +
                "                // C++版本暂不支持直接切换到父级frame\n" +
                "                driver.SwitchTo().DefaultContent();\n" +
                "                std::cout << \"✅ 已切换回默认内容\" << std::endl;\n" +
                "            } else {\n" +
                "                auto iframeElement = driver.FindElement(webdriverxx::ByXPath(resolvedXpath));\n" +
                "                driver.SwitchTo().Frame(iframeElement);\n" +
                "                std::cout << \"✅ 已切换到iframe: \" << resolvedXpath << std::endl;\n" +
                "            }\n" +
                "        }\n";
    }

    private String generateGetCurrentUrl(Map<String, Object> step) {
        String key = (String) step.get("value");
        if (key == null) {
            key = "current_url";
        }
        return "        {\n" +
                "            std::string currentUrl = driver.GetCurrentUrl();\n" +
                "            context[\"" + key + "\"] = 0; // C++版本上下文只存储int类型\n" +
                "            std::cout << \"🌐 获取当前URL: \" << currentUrl << std::endl;\n" +
                "            std::cout << \"💾 已保存到上下文: " + key + " = \" << currentUrl << std::endl;\n" +
                "        }\n";
    }

    private String generatePressKeys(Map<String, Object> step) {
        String keys = (String) step.get("value");
        return "        {\n" +
                "            std::string resolvedValue = resolveValueWithExpression(\"" + keys + "\", context);\n" +
                "            auto actions = driver.Actions();\n" +
                generateKeyActions("resolvedValue") +
                "            actions.Perform();\n" +
                "            std::cout << \"⌨️ 模拟按键: \" << resolvedValue << std::endl;\n" +
                "        }\n";
    }

    private String generateKeyboardInput(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        String value = (String) step.get("value");
        return "        {\n" +
                "            std::string resolvedXpath = resolveXpathWithExpression(\"" + xpath + "\", context);\n" +
                "            std::string resolvedValue = resolveValueWithExpression(\"" + value + "\", context);\n" +
                "            auto actions = driver.Actions();\n" +
                "            auto element = driver.FindElement(webdriverxx::ByXPath(resolvedXpath));\n" +
                "            element.Click();\n" +
                "            // C++版本暂不支持复杂的键盘输入序列\n" +
                "            element.SendKeys(resolvedValue);\n" +
                "            std::cout << \"⌨️ 键盘输入: '\" << resolvedValue << \"' 到元素: \" << resolvedXpath << std::endl;\n" +
                "        }\n";
    }

    private String generateGoBack(Map<String, Object> step) {
        return "        driver.GoBack();\n" +
                "        std::cout << \"↩️ 返回上一页\" << std::endl;\n";
    }

    private String generateHandleAlert(Map<String, Object> step) {
        Boolean acceptAlert = (Boolean) step.get("acceptAlert");
        String alertText = (String) step.get("alertText");

        StringBuilder alertCode = new StringBuilder();
        alertCode.append("        try {\n");
        alertCode.append("            auto alert = driver.SwitchTo().Alert();\n");

        if (alertText != null && !alertText.isEmpty()) {
            alertCode.append("            alert.SendKeys(\"").append(alertText).append("\");\n");
            alertCode.append("            std::cout << \"⌨️ 在弹窗中输入文本: ").append(alertText).append("\" << std::endl;\n");
        }

        if (acceptAlert != null) {
            if (acceptAlert) {
                alertCode.append("            alert.Accept();\n");
                alertCode.append("            std::cout << \"✅ 接受弹窗\" << std::endl;\n");
            } else {
                alertCode.append("            alert.Dismiss();\n");
                alertCode.append("            std::cout << \"❌ 取消弹窗\" << std::endl;\n");
            }
        }

        alertCode.append("        } catch (const std::exception& e) {\n");
        alertCode.append("            std::cout << \"⚠️ 未检测到弹窗\" << std::endl;\n");
        alertCode.append("        }\n");

        return alertCode.toString();
    }

    private String generateCloseTab(Map<String, Object> step) {
        return "        {\n" +
                "            std::string currentHandle = driver.GetWindowHandle();\n" +
                "            auto handles = driver.GetWindowHandles();\n" +
                "            if (handles.size() > 1) {\n" +
                "                driver.Close();\n" +
                "                handles = driver.GetWindowHandles();\n" +
                "                if (!handles.empty()) {\n" +
                "                    std::string newHandle = *handles.begin();\n" +
                "                    driver.SwitchTo().Window(newHandle);\n" +
                "                    std::string currentUrl = driver.GetCurrentUrl();\n" +
                "                    std::cout << \"✅ 关闭标签页，切换到新标签页\" << std::endl;\n" +
                "                    std::cout << \"🌐 当前URL: \" << currentUrl << std::endl;\n" +
                "                }\n" +
                "            } else {\n" +
                "                std::cout << \"⚠️ 只有一个标签页，无法关闭\" << std::endl;\n" +
                "            }\n" +
                "        }\n";
    }

    private String generateImportCookie(Map<String, Object> step) {
        String filePath = (String) step.get("filePath");
        return "        {\n" +
                "            std::cout << \"🍪 开始从文件导入Cookie: " + filePath + "\" << std::endl;\n" +
                "            // C++版本Cookie导入需要手动实现JSON解析\n" +
                "            std::cout << \"⚠️ C++版本需要手动实现Cookie导入功能\" << std::endl;\n" +
                "            std::cout << \"📁 Cookie文件路径: " + filePath + "\" << std::endl;\n" +
                "            // 刷新页面\n" +
                "            driver.Refresh();\n" +
                "            std::cout << \"🔄 已刷新页面使Cookie生效\" << std::endl;\n" +
                "            safeWait(2000);\n" +
                "        }\n";
    }

    private String generateKeyActions(String keyVariable) {
        return "            std::vector<std::string> keySequence;\n" +
                "            size_t start = 0;\n" +
                "            size_t end = " + keyVariable + ".find('+');\n" +
                "            while (end != std::string::npos) {\n" +
                "                keySequence.push_back(" + keyVariable + ".substr(start, end - start));\n" +
                "                start = end + 1;\n" +
                "                end = " + keyVariable + ".find('+', start);\n" +
                "            }\n" +
                "            keySequence.push_back(" + keyVariable + ".substr(start));\n" +
                "            \n" +
                "            for (const auto& key : keySequence) {\n" +
                "                std::string trimmedKey = key;\n" +
                "                trimmedKey.erase(0, trimmedKey.find_first_not_of(\" \\t\\n\\r\\f\\v\"));\n" +
                "                trimmedKey.erase(trimmedKey.find_last_not_of(\" \\t\\n\\r\\f\\v\") + 1);\n" +
                "                std::transform(trimmedKey.begin(), trimmedKey.end(), trimmedKey.begin(), ::toupper);\n" +
                "                \n" +
                "                if (trimmedKey == \"CTRL\") {\n" +
                "                    actions.KeyDown(webdriverxx::Keys::CONTROL);\n" +
                "                } else if (trimmedKey == \"SHIFT\") {\n" +
                "                    actions.KeyDown(webdriverxx::Keys::SHIFT);\n" +
                "                } else if (trimmedKey == \"ALT\") {\n" +
                "                    actions.KeyDown(webdriverxx::Keys::ALT);\n" +
                "                } else if (trimmedKey == \"ENTER\") {\n" +
                "                    actions.SendKeys(webdriverxx::Keys::ENTER);\n" +
                "                } else if (trimmedKey == \"TAB\") {\n" +
                "                    actions.SendKeys(webdriverxx::Keys::TAB);\n" +
                "                } else if (trimmedKey == \"ESC\") {\n" +
                "                    actions.SendKeys(webdriverxx::Keys::ESCAPE);\n" +
                "                } else if (trimmedKey == \"BACKSPACE\") {\n" +
                "                    actions.SendKeys(webdriverxx::Keys::BACKSPACE);\n" +
                "                } else if (trimmedKey == \"DELETE\") {\n" +
                "                    actions.SendKeys(webdriverxx::Keys::DELETE);\n" +
                "                } else if (trimmedKey == \"HOME\") {\n" +
                "                    actions.SendKeys(webdriverxx::Keys::HOME);\n" +
                "                } else if (trimmedKey == \"END\") {\n" +
                "                    actions.SendKeys(webdriverxx::Keys::END);\n" +
                "                } else if (trimmedKey == \"PAGEUP\") {\n" +
                "                    actions.SendKeys(webdriverxx::Keys::PAGE_UP);\n" +
                "                } else if (trimmedKey == \"PAGEDOWN\") {\n" +
                "                    actions.SendKeys(webdriverxx::Keys::PAGE_DOWN);\n" +
                "                } else if (trimmedKey == \"ARROW_UP\") {\n" +
                "                    actions.SendKeys(webdriverxx::Keys::ARROW_UP);\n" +
                "                } else if (trimmedKey == \"ARROW_DOWN\") {\n" +
                "                    actions.SendKeys(webdriverxx::Keys::ARROW_DOWN);\n" +
                "                } else if (trimmedKey == \"ARROW_LEFT\") {\n" +
                "                    actions.SendKeys(webdriverxx::Keys::ARROW_LEFT);\n" +
                "                } else if (trimmedKey == \"ARROW_RIGHT\") {\n" +
                "                    actions.SendKeys(webdriverxx::Keys::ARROW_RIGHT);\n" +
                "                } else {\n" +
                "                    if (trimmedKey.length() == 2 && trimmedKey[0] == 'F') {\n" +
                "                        char fn = trimmedKey[1];\n" +
                "                        if (fn >= '1' && fn <= '9') {\n" +
                "                            actions.SendKeys(webdriverxx::Keys::" + generateFKeyCases() + ");\n" +
                "                        }\n" +
                "                    } else if (trimmedKey.length() == 1 && std::isalpha(trimmedKey[0])) {\n" +
                "                        actions.SendKeys(trimmedKey);\n" +
                "                    } else {\n" +
                "                        std::cout << \"⚠️ 不支持的按键: \" << trimmedKey << std::endl;\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "            actions.KeyUp(webdriverxx::Keys::CONTROL).KeyUp(webdriverxx::Keys::SHIFT).KeyUp(webdriverxx::Keys::ALT);\n";
    }

    private String generateFKeyCases() {
        StringBuilder fKeys = new StringBuilder();
        for (int i = 1; i <= 12; i++) {
            fKeys.append("if (fn == '").append(i).append("') { return webdriverxx::Keys::F").append(i).append("; } else ");
        }
        fKeys.append("{ return \"\"; }");
        return fKeys.toString();
    }

    private String generateHelperMethods() {
        return "void safeWait(int milliseconds) {\n" +
                "    std::this_thread::sleep_for(std::chrono::milliseconds(milliseconds));\n" +
                "}\n\n" +
                "std::string resolveXpathWithExpression(const std::string& xpath, const std::map<std::string, int>& context) {\n" +
                "    if (xpath.empty()) return xpath;\n" +
                "    std::string result = xpath;\n" +
                "    std::regex pattern(\"\\\\{([^}]+)\\\\}\");\n" +
                "    std::smatch matches;\n" +
                "    \n" +
                "    std::string::const_iterator searchStart(xpath.cbegin());\n" +
                "    while (std::regex_search(searchStart, xpath.cend(), matches, pattern)) {\n" +
                "        std::string fullMatch = matches[0];\n" +
                "        std::string expression = matches[1];\n" +
                "        try {\n" +
                "            int value = parseExpression(fullMatch, context);\n" +
                "            size_t pos = result.find(fullMatch);\n" +
                "            if (pos != std::string::npos) {\n" +
                "                result.replace(pos, fullMatch.length(), std::to_string(value));\n" +
                "            }\n" +
                "        } catch (const std::exception& e) {\n" +
                "            // 解析失败，保持原样\n" +
                "        }\n" +
                "        searchStart = matches.suffix().first;\n" +
                "    }\n" +
                "    return result;\n" +
                "}\n\n" +
                "std::string resolveValueWithExpression(const std::string& value, const std::map<std::string, int>& context) {\n" +
                "    if (value.empty()) return value;\n" +
                "    std::string result = value;\n" +
                "    std::regex pattern(\"\\\\{([^}]+)\\\\}\");\n" +
                "    std::smatch matches;\n" +
                "    \n" +
                "    std::string::const_iterator searchStart(value.cbegin());\n" +
                "    while (std::regex_search(searchStart, value.cend(), matches, pattern)) {\n" +
                "        std::string fullMatch = matches[0];\n" +
                "        std::string expression = matches[1];\n" +
                "        try {\n" +
                "            int exprValue = parseExpression(fullMatch, context);\n" +
                "            size_t pos = result.find(fullMatch);\n" +
                "            if (pos != std::string::npos) {\n" +
                "                result.replace(pos, fullMatch.length(), std::to_string(exprValue));\n" +
                "            }\n" +
                "        } catch (const std::exception& e) {\n" +
                "            // 解析失败，保持原样\n" +
                "        }\n" +
                "        searchStart = matches.suffix().first;\n" +
                "    }\n" +
                "    return result;\n" +
                "}\n\n" +
                "int parseExpression(const std::string& expression, const std::map<std::string, int>& context) {\n" +
                "    std::string expr = expression;\n" +
                "    expr.erase(0, 1); // 移除开头的 {\n" +
                "    expr.erase(expr.length() - 1); // 移除结尾的 }\n" +
                "    \n" +
                "    // 去除首尾空格\n" +
                "    expr.erase(0, expr.find_first_not_of(\" \\t\\n\\r\\f\\v\"));\n" +
                "    expr.erase(expr.find_last_not_of(\" \\t\\n\\r\\f\\v\") + 1);\n" +
                "    \n" +
                "    // 检查是否是纯数字\n" +
                "    if (std::regex_match(expr, std::regex(\"^\\\\d+$\"))) {\n" +
                "        return std::stoi(expr);\n" +
                "    }\n" +
                "    \n" +
                "    // 检查是否包含变量和运算符\n" +
                "    for (const auto& pair : context) {\n" +
                "        const std::string& varName = pair.first;\n" +
                "        if (expr.find(varName) == 0) {\n" +
                "            int baseValue = pair.second;\n" +
                "            std::string operatorPart = expr.substr(varName.length());\n" +
                "            \n" +
                "            // 去除运算符部分的首尾空格\n" +
                "            operatorPart.erase(0, operatorPart.find_first_not_of(\" \\t\\n\\r\\f\\v\"));\n" +
                "            operatorPart.erase(operatorPart.find_last_not_of(\" \\t\\n\\r\\f\\v\") + 1);\n" +
                "            \n" +
                "            if (operatorPart.empty()) {\n" +
                "                return baseValue;\n" +
                "            }\n" +
                "            \n" +
                "            if (std::regex_match(operatorPart, std::regex(\"^[+\\\\-*/]\\\\s*\\\\d+$\"))) {\n" +
                "                char operatorChar = operatorPart[0];\n" +
                "                std::string numberStr = operatorPart.substr(1);\n" +
                "                numberStr.erase(0, numberStr.find_first_not_of(\" \\t\\n\\r\\f\\v\"));\n" +
                "                int number = std::stoi(numberStr);\n" +
                "                \n" +
                "                switch (operatorChar) {\n" +
                "                    case '+': return baseValue + number;\n" +
                "                    case '-': return baseValue - number;\n" +
                "                    case '*': return baseValue * number;\n" +
                "                    case '/': return baseValue / number;\n" +
                "                    default: return baseValue;\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    \n" +
                "    return 1;\n" +
                "}\n\n" +
                "std::vector<std::string> parseKeySequence(const std::string& input) {\n" +
                "    std::vector<std::string> sequence;\n" +
                "    std::string currentText;\n" +
                "    \n" +
                "    for (size_t i = 0; i < input.length(); i++) {\n" +
                "        char c = input[i];\n" +
                "        \n" +
                "        if (c == '{' && i + 1 < input.length()) {\n" +
                "            size_t endIndex = input.find('}', i);\n" +
                "            if (endIndex != std::string::npos) {\n" +
                "                std::string specialKey = input.substr(i + 1, endIndex - i - 1);\n" +
                "                std::transform(specialKey.begin(), specialKey.end(), specialKey.begin(), ::toupper);\n" +
                "                \n" +
                "                if (!currentText.empty()) {\n" +
                "                    sequence.push_back(currentText);\n" +
                "                    currentText.clear();\n" +
                "                }\n" +
                "                \n" +
                "                if (specialKey == \"ENTER\") {\n" +
                "                    sequence.push_back(\"\\\\uE007\"); // Keys::ENTER\n" +
                "                } else if (specialKey == \"TAB\") {\n" +
                "                    sequence.push_back(\"\\\\uE004\"); // Keys::TAB\n" +
                "                } else if (specialKey == \"ESC\" || specialKey == \"ESCAPE\") {\n" +
                "                    sequence.push_back(\"\\\\uE00C\"); // Keys::ESCAPE\n" +
                "                } else if (specialKey == \"BACKSPACE\") {\n" +
                "                    sequence.push_back(\"\\\\uE003\"); // Keys::BACKSPACE\n" +
                "                } else if (specialKey == \"DELETE\") {\n" +
                "                    sequence.push_back(\"\\\\uE017\"); // Keys::DELETE\n" +
                "                } else if (specialKey == \"HOME\") {\n" +
                "                    sequence.push_back(\"\\\\uE011\"); // Keys::HOME\n" +
                "                } else if (specialKey == \"END\") {\n" +
                "                    sequence.push_back(\"\\\\uE010\"); // Keys::END\n" +
                "                } else if (specialKey == \"PAGEUP\") {\n" +
                "                    sequence.push_back(\"\\\\uE00E\"); // Keys::PAGE_UP\n" +
                "                } else if (specialKey == \"PAGEDOWN\") {\n" +
                "                    sequence.push_back(\"\\\\uE00F\"); // Keys::PAGE_DOWN\n" +
                "                } else if (specialKey == \"UP\" || specialKey == \"ARROW_UP\") {\n" +
                "                    sequence.push_back(\"\\\\uE013\"); // Keys::ARROW_UP\n" +
                "                } else if (specialKey == \"DOWN\" || specialKey == \"ARROW_DOWN\") {\n" +
                "                    sequence.push_back(\"\\\\uE015\"); // Keys::ARROW_DOWN\n" +
                "                } else if (specialKey == \"LEFT\" || specialKey == \"ARROW_LEFT\") {\n" +
                "                    sequence.push_back(\"\\\\uE012\"); // Keys::ARROW_LEFT\n" +
                "                } else if (specialKey == \"RIGHT\" || specialKey == \"ARROW_RIGHT\") {\n" +
                "                    sequence.push_back(\"\\\\uE014\"); // Keys::ARROW_RIGHT\n" +
                "                } else if (specialKey == \"CTRL\") {\n" +
                "                    sequence.push_back(\"\\\\uE009\"); // Keys::CONTROL\n" +
                "                } else if (specialKey == \"SHIFT\") {\n" +
                "                    sequence.push_back(\"\\\\uE008\"); // Keys::SHIFT\n" +
                "                } else if (specialKey == \"ALT\") {\n" +
                "                    sequence.push_back(\"\\\\uE00A\"); // Keys::ALT\n" +
                "                } else {\n" +
                "                    if (std::regex_match(specialKey, std::regex(\"F[1-9]|F1[0-2]\"))) {\n" +
                "                        sequence.push_back(\"\\\\uE0\" + specialKey.substr(1));\n" +
                "                    } else {\n" +
                "                        currentText += \"{\" + specialKey + \"}\";\n" +
                "                    }\n" +
                "                }\n" +
                "                i = endIndex;\n" +
                "            } else {\n" +
                "                currentText += c;\n" +
                "            }\n" +
                "        } else {\n" +
                "            currentText += c;\n" +
                "        }\n" +
                "    }\n" +
                "    \n" +
                "    if (!currentText.empty()) {\n" +
                "        sequence.push_back(currentText);\n" +
                "    }\n" +
                "    \n" +
                "    return sequence;\n" +
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
        return "cpp";
    }

    @Override
    public String getFileExtension() {
        return ".cpp";
    }
}