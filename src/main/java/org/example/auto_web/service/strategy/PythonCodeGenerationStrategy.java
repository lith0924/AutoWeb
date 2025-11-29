package org.example.auto_web.service.strategy;

import org.example.auto_web.service.CodeGenerationStrategy;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class PythonCodeGenerationStrategy implements CodeGenerationStrategy {

    @Override
    public String generateCode(List<Map<String, Object>> steps, String initialUrl, String className, Boolean includeComments) {
        StringBuilder code = new StringBuilder();

        // 导入语句
        code.append("from selenium import webdriver\n");
        code.append("from selenium.webdriver.common.by import By\n");
        code.append("from selenium.webdriver.support.ui import WebDriverWait\n");
        code.append("from selenium.webdriver.support import expected_conditions as EC\n");
        code.append("from selenium.webdriver.common.keys import Keys\n");
        code.append("from selenium.webdriver.common.action_chains import ActionChains\n");
        code.append("import time\n");
        code.append("import json\n");
        code.append("import re\n\n");

        // 注释
        if (includeComments) {
            code.append("\"\"\"\n");
            code.append("自动生成的Selenium测试脚本\n");
            code.append("生成时间: ").append(new Date()).append("\n");
            code.append("步骤数量: ").append(steps.size()).append("\n");
            code.append("\"\"\"\n\n");
        }

        // 主函数
        code.append("def ").append(className.toLowerCase()).append("():\n");
        code.append("    driver = webdriver.Chrome()\n");
        code.append("    context = {}\n");
        code.append("    \n");
        code.append("    try:\n");

        // 初始导航
        if (initialUrl != null && !initialUrl.isEmpty()) {
            code.append("        # 初始导航\n");
            code.append("        driver.get(\"").append(initialUrl).append("\")\n");
            code.append("        print(\"✅ 初始导航到: ").append(initialUrl).append("\")\n");
            code.append("        time.sleep(2)\n\n");
        }

        // 生成步骤代码
        for (int i = 0; i < steps.size(); i++) {
            Map<String, Object> step = steps.get(i);
            code.append(generateStepCode(step, i + 1, includeComments));
            code.append("\n");
        }

        code.append("        print(\"🎉 所有操作执行完成\")\n");
        code.append("        \n");
        code.append("    except Exception as e:\n");
        code.append("        print(f\"❌ 执行失败: {e}\")\n");
        code.append("    finally:\n");
        code.append("        driver.quit()\n");
        code.append("        print(\"🔚 浏览器已关闭\")\n\n");

        // 辅助方法
        code.append(generateHelperMethods());

        code.append("if __name__ == \"__main__\":\n");
        code.append("    ").append(className.toLowerCase()).append("()\n");

        return code.toString();
    }

    private String generateStepCode(Map<String, Object> step, int stepNumber, Boolean includeComments) {
        String type = (String) step.get("type");
        String remark = (String) step.get("remark");
        StringBuilder stepCode = new StringBuilder();

        // 步骤注释
        if (includeComments) {
            stepCode.append("        # 步骤 ").append(stepNumber);
            if (remark != null && !remark.isEmpty()) {
                stepCode.append(": ").append(remark);
            }
            stepCode.append("\n");
        }

        // 操作前等待
        Long waitBeforeMs = getLongValue(step, "waitBeforeMs");
        if (waitBeforeMs != null && waitBeforeMs > 0) {
            stepCode.append("        safe_wait(").append(waitBeforeMs).append(")\n");
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
                stepCode.append("        # 不支持的操作类型: ").append(type).append("\n");
        }

        // 操作后等待
        Long waitAfterMs = getLongValue(step, "waitAfterMs");
        if (waitAfterMs != null && waitAfterMs > 0) {
            stepCode.append("        safe_wait(").append(waitAfterMs).append(")\n");
        }

        return stepCode.toString();
    }

    private String generateClick(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        return "        resolved_xpath = resolve_xpath_with_expression(\"" + xpath + "\", context)\n" +
                "        element = WebDriverWait(driver, 10).until(\n" +
                "            EC.element_to_be_clickable((By.XPATH, resolved_xpath))\n" +
                "        )\n" +
                "        element.click()\n" +
                "        print(f\"✅ 点击元素: {resolved_xpath}\")\n";
    }

    private String generateInput(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        String value = (String) step.get("value");
        return "        resolved_xpath = resolve_xpath_with_expression(\"" + xpath + "\", context)\n" +
                "        resolved_value = resolve_value_with_expression(\"" + value + "\", context)\n" +
                "        element = WebDriverWait(driver, 10).until(\n" +
                "            EC.presence_of_element_located((By.XPATH, resolved_xpath))\n" +
                "        )\n" +
                "        element.clear()\n" +
                "        element.send_keys(resolved_value)\n" +
                "        print(f\"✅ 输入内容: '{resolved_value}' 到元素: {resolved_xpath}\")\n";
    }

    private String generateGetText(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        return "        resolved_xpath = resolve_xpath_with_expression(\"" + xpath + "\", context)\n" +
                "        element = WebDriverWait(driver, 10).until(\n" +
                "            EC.presence_of_element_located((By.XPATH, resolved_xpath))\n" +
                "        )\n" +
                "        text = element.text\n" +
                "        print(f\"📖 获取文本: '{text}' 从元素: {resolved_xpath}\")\n";
    }

    private String generateLoopClick(Map<String, Object> step) {
        Integer startIndex = getIntegerValue(step, "startIndex", 1);
        Integer endIndex = getIntegerValue(step, "endIndex", 1);
        Integer increment = getIntegerValue(step, "increment", 1);
        String xpath = (String) step.get("xpath");

        return "        print(f\"🔄 开始遍历点击操作，范围: " + startIndex + " - " + endIndex + "，增量: " + increment + "\")\n" +
                "        for i in range(" + startIndex + ", " + endIndex + " + 1, " + increment + "):\n" +
                "            context['i'] = i\n" +
                "            context['index'] = i\n" +
                "            context['current'] = i\n" +
                "            resolved_xpath = resolve_xpath_with_expression(\"" + xpath + "\", context)\n" +
                "            try:\n" +
                "                element = WebDriverWait(driver, 10).until(\n" +
                "                    EC.element_to_be_clickable((By.XPATH, resolved_xpath))\n" +
                "                )\n" +
                "                element.click()\n" +
                "                print(f\"✅ 遍历点击成功: 索引: {i}, XPath: {resolved_xpath}\")\n" +
                "                safe_wait(500)\n" +
                "            except Exception as e:\n" +
                "                print(f\"⚠️ 遍历点击失败，索引: {i}, XPath: {resolved_xpath}\")\n" +
                "        context.pop('i', None)\n" +
                "        context.pop('index', None)\n" +
                "        context.pop('current', None)\n";
    }

    private String generateLoopInput(Map<String, Object> step) {
        Integer startIndex = getIntegerValue(step, "startIndex", 1);
        Integer endIndex = getIntegerValue(step, "endIndex", 1);
        Integer increment = getIntegerValue(step, "increment", 1);
        String xpath = (String) step.get("xpath");
        String value = (String) step.get("value");

        return "        print(f\"🔄 开始遍历输入操作，范围: " + startIndex + " - " + endIndex + "，增量: " + increment + "\")\n" +
                "        for i in range(" + startIndex + ", " + endIndex + " + 1, " + increment + "):\n" +
                "            context['i'] = i\n" +
                "            context['index'] = i\n" +
                "            context['current'] = i\n" +
                "            resolved_xpath = resolve_xpath_with_expression(\"" + xpath + "\", context)\n" +
                "            resolved_value = resolve_value_with_expression(\"" + value + "\", context)\n" +
                "            try:\n" +
                "                element = WebDriverWait(driver, 10).until(\n" +
                "                    EC.presence_of_element_located((By.XPATH, resolved_xpath))\n" +
                "                )\n" +
                "                element.clear()\n" +
                "                element.send_keys(resolved_value)\n" +
                "                print(f\"✅ 遍历输入成功: 索引: {i}, 值: '{resolved_value}', XPath: {resolved_xpath}\")\n" +
                "                safe_wait(500)\n" +
                "            except Exception as e:\n" +
                "                print(f\"⚠️ 遍历输入失败，索引: {i}, XPath: {resolved_xpath}\")\n" +
                "        context.pop('i', None)\n" +
                "        context.pop('index', None)\n" +
                "        context.pop('current', None)\n";
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

        return "        print(f\"🔄 开始遍历获取文本操作，范围: " + startIndex + " - " + endIndex + "，增量: " + increment + "\")\n" +
                "        text_list = []\n" +
                "        for i in range(" + startIndex + ", " + endIndex + " + 1, " + increment + "):\n" +
                "            context['i'] = i\n" +
                "            context['index'] = i\n" +
                "            context['current'] = i\n" +
                "            resolved_xpath = resolve_xpath_with_expression(\"" + xpath + "\", context)\n" +
                "            try:\n" +
                "                element = WebDriverWait(driver, 10).until(\n" +
                "                    EC.presence_of_element_located((By.XPATH, resolved_xpath))\n" +
                "                )\n" +
                "                text = element.text.strip()\n" +
                "                text_list.append(text)\n" +
                "                print(f\"📖 获取文本 [{i}]: '{text}'\")\n" +
                "                safe_wait(500)\n" +
                "            except Exception as e:\n" +
                "                print(f\"⚠️ 获取文本失败，索引: {i}\")\n" +
                "                text_list.append(\"\")\n" +
                "        context.pop('i', None)\n" +
                "        context.pop('index', None)\n" +
                "        context.pop('current', None)\n" +
                "        # 写入文件\n" +
                "        try:\n" +
                "            with open(\"" + filePath + "\", 'w', encoding='utf-8') as f:\n" +
                "                for text in text_list:\n" +
                "                    f.write(text + '\\n')\n" +
                "            print(f\"💾 成功将 {len(text_list)} 条文本写入文件: " + filePath + "\")\n" +
                "        except Exception as e:\n" +
                "            print(f\"❌ 写入文件失败: " + filePath + "\")\n";
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
        loopCode.append("        print(f\"🔄 开始循环任务，迭代次数: ").append(iterations).append("，增量: ").append(increment).append("\")\n");
        loopCode.append("        for ").append(loopVar).append(" in range(0, ").append(iterations).append(", ").append(increment).append("):\n");
        loopCode.append("            context['").append(loopVar).append("'] = ").append(loopVar).append("\n");
        loopCode.append("            context['i'] = ").append(loopVar).append("\n");
        loopCode.append("            context['index'] = ").append(loopVar).append("\n");
        loopCode.append("            print(f\"🔄 循环任务迭代: {\"").append(loopVar).append("\" + 1}/").append(iterations).append("\")\n");

        // 生成子步骤代码
        if (subSteps != null) {
            for (int i = 0; i < subSteps.size(); i++) {
                Map<String, Object> subStep = subSteps.get(i);
                String subStepCode = generateStepCode(subStep, i + 1, false)
                        .replace("        ", "            ");
                loopCode.append(subStepCode).append("\n");
            }
        }

        loopCode.append("        context.pop('").append(loopVar).append("', None)\n");
        loopCode.append("        context.pop('i', None)\n");
        loopCode.append("        context.pop('index', None)\n");
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
        loopCode.append("        print(f\"🔄 开始动态循环，迭代次数: ").append(iterations).append("，增量: ").append(increment).append("\")\n");
        loopCode.append("        for ").append(loopVar).append(" in range(0, ").append(iterations).append(", ").append(increment).append("):\n");
        loopCode.append("            context['").append(loopVar).append("'] = ").append(loopVar).append("\n");
        loopCode.append("            context['i'] = ").append(loopVar).append("\n");
        loopCode.append("            context['index'] = ").append(loopVar).append("\n");
        loopCode.append("            print(f\"🔄 动态循环迭代: {\"").append(loopVar).append("\" + 1}/").append(iterations).append("\")\n");

        // 生成子步骤代码
        if (subSteps != null) {
            for (int i = 0; i < subSteps.size(); i++) {
                Map<String, Object> subStep = subSteps.get(i);
                String subStepCode = generateStepCode(subStep, i + 1, false)
                        .replace("        ", "            ");
                loopCode.append(subStepCode).append("\n");
            }
        }

        loopCode.append("            safe_wait(1000)\n");
        loopCode.append("        context.pop('").append(loopVar).append("', None)\n");
        loopCode.append("        context.pop('i', None)\n");
        loopCode.append("        context.pop('index', None)\n");
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
        return "        print(f\"⏳ 等待 " + waitTime + " 毫秒\")\n" +
                "        safe_wait(" + waitTime + ")\n";
    }

    private String generateNavigate(Map<String, Object> step) {
        String url = (String) step.get("value");
        return "        driver.get(\"" + url + "\")\n" +
                "        print(f\"🌐 导航到: " + url + "\")\n";
    }

    private String generateSwitchIframe(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        return "        resolved_xpath = resolve_xpath_with_expression(\"" + xpath + "\", context)\n" +
                "        if resolved_xpath.lower() == \"default\":\n" +
                "            driver.switch_to.default_content()\n" +
                "            print(\"✅ 已切换回默认内容\")\n" +
                "        elif resolved_xpath.lower() == \"parent\":\n" +
                "            driver.switch_to.parent_frame()\n" +
                "            print(\"✅ 已切换回父级iframe\")\n" +
                "        else:\n" +
                "            iframe_element = WebDriverWait(driver, 10).until(\n" +
                "                EC.presence_of_element_located((By.XPATH, resolved_xpath))\n" +
                "            )\n" +
                "            driver.switch_to.frame(iframe_element)\n" +
                "            print(f\"✅ 已切换到iframe: {resolved_xpath}\")\n";
    }

    private String generateGetCurrentUrl(Map<String, Object> step) {
        String key = (String) step.get("value");
        if (key == null) {
            key = "current_url";
        }
        return "        current_url = driver.current_url\n" +
                "        context['" + key + "'] = current_url\n" +
                "        print(f\"🌐 获取当前URL: {current_url}\")\n" +
                "        print(f\"💾 已保存到上下文: " + key + " = {current_url}\")\n";
    }

    private String generatePressKeys(Map<String, Object> step) {
        String keys = (String) step.get("value");
        return "        resolved_value = resolve_value_with_expression(\"" + keys + "\", context)\n" +
                "        actions = ActionChains(driver)\n" +
                generateKeyActions("resolved_value") +
                "        actions.perform()\n" +
                "        print(f\"⌨️ 模拟按键: {resolved_value}\")\n";
    }

    private String generateKeyboardInput(Map<String, Object> step) {
        String xpath = (String) step.get("xpath");
        String value = (String) step.get("value");
        return "        resolved_xpath = resolve_xpath_with_expression(\"" + xpath + "\", context)\n" +
                "        resolved_value = resolve_value_with_expression(\"" + value + "\", context)\n" +
                "        actions = ActionChains(driver)\n" +
                "        element = WebDriverWait(driver, 10).until(\n" +
                "            EC.element_to_be_clickable((By.XPATH, resolved_xpath))\n" +
                "        )\n" +
                "        element.click()\n" +
                "        actions.send_keys(parse_key_sequence(resolved_value))\n" +
                "        actions.perform()\n" +
                "        print(f\"⌨️ 键盘输入: '{resolved_value}' 到元素: {resolved_xpath}\")\n";
    }

    private String generateGoBack(Map<String, Object> step) {
        return "        driver.back()\n" +
                "        print(\"↩️ 返回上一页\")\n";
    }

    private String generateHandleAlert(Map<String, Object> step) {
        Boolean acceptAlert = (Boolean) step.get("acceptAlert");
        String alertText = (String) step.get("alertText");

        StringBuilder alertCode = new StringBuilder();
        alertCode.append("        try:\n");
        alertCode.append("            WebDriverWait(driver, 5).until(EC.alert_is_present())\n");
        alertCode.append("            alert = driver.switch_to.alert\n");

        if (alertText != null && !alertText.isEmpty()) {
            alertCode.append("            alert.send_keys(\"").append(alertText).append("\")\n");
            alertCode.append("            print(f\"⌨️ 在弹窗中输入文本: ").append(alertText).append("\")\n");
        }

        if (acceptAlert != null) {
            if (acceptAlert) {
                alertCode.append("            alert.accept()\n");
                alertCode.append("            print(\"✅ 接受弹窗\")\n");
            } else {
                alertCode.append("            alert.dismiss()\n");
                alertCode.append("            print(\"❌ 取消弹窗\")\n");
            }
        }

        alertCode.append("        except Exception as e:\n");
        alertCode.append("            print(\"⚠️ 未检测到弹窗\")\n");

        return alertCode.toString();
    }

    private String generateCloseTab(Map<String, Object> step) {
        return "        current_handle = driver.current_window_handle\n" +
                "        handles = driver.window_handles\n" +
                "        if len(handles) > 1:\n" +
                "            driver.close()\n" +
                "            handles = driver.window_handles\n" +
                "            if handles:\n" +
                "                new_handle = handles[0]\n" +
                "                driver.switch_to.window(new_handle)\n" +
                "                print(\"✅ 关闭标签页，切换到新标签页\")\n" +
                "                print(f\"🌐 当前URL: {driver.current_url}\")\n" +
                "        else:\n" +
                "            print(\"⚠️ 只有一个标签页，无法关闭\")\n";
    }

    private String generateImportCookie(Map<String, Object> step) {
        String filePath = (String) step.get("filePath");
        return "        try:\n" +
                "            print(f\"🍪 开始从文件导入Cookie: " + filePath + "\")\n" +
                "            with open(\"" + filePath + "\", 'r', encoding='utf-8') as f:\n" +
                "                cookie_json = f.read()\n" +
                "            cookies = json.loads(cookie_json)\n" +
                "            \n" +
                "            imported_count = 0\n" +
                "            for cookie_data in cookies:\n" +
                "                try:\n" +
                "                    driver.add_cookie(cookie_data)\n" +
                "                    imported_count += 1\n" +
                "                    print(f\"✅ 导入Cookie: {cookie_data.get('name')}\")\n" +
                "                except Exception as e:\n" +
                "                    print(f\"⚠️ 导入单个Cookie失败: {cookie_data.get('name')}\")\n" +
                "            \n" +
                "            print(f\"✅ 成功导入 {imported_count} 个Cookie\")\n" +
                "            \n" +
                "            # 刷新页面使Cookie生效\n" +
                "            driver.refresh()\n" +
                "            print(\"🔄 已刷新页面使Cookie生效\")\n" +
                "            safe_wait(2000)\n" +
                "        except Exception as e:\n" +
                "            print(f\"❌ 导入Cookie失败: {e}\")\n";
    }

    private String generateKeyActions(String keyVariable) {
        return "        key_sequence = " + keyVariable + ".split('+')\n" +
                "        for key in key_sequence:\n" +
                "            key = key.strip().upper()\n" +
                "            if key == 'CTRL':\n" +
                "                actions.key_down(Keys.CONTROL)\n" +
                "            elif key == 'SHIFT':\n" +
                "                actions.key_down(Keys.SHIFT)\n" +
                "            elif key == 'ALT':\n" +
                "                actions.key_down(Keys.ALT)\n" +
                "            elif key == 'ENTER':\n" +
                "                actions.send_keys(Keys.ENTER)\n" +
                "            elif key == 'TAB':\n" +
                "                actions.send_keys(Keys.TAB)\n" +
                "            elif key == 'ESC':\n" +
                "                actions.send_keys(Keys.ESCAPE)\n" +
                "            elif key == 'BACKSPACE':\n" +
                "                actions.send_keys(Keys.BACKSPACE)\n" +
                "            elif key == 'DELETE':\n" +
                "                actions.send_keys(Keys.DELETE)\n" +
                "            elif key == 'HOME':\n" +
                "                actions.send_keys(Keys.HOME)\n" +
                "            elif key == 'END':\n" +
                "                actions.send_keys(Keys.END)\n" +
                "            elif key == 'PAGEUP':\n" +
                "                actions.send_keys(Keys.PAGE_UP)\n" +
                "            elif key == 'PAGEDOWN':\n" +
                "                actions.send_keys(Keys.PAGE_DOWN)\n" +
                "            elif key == 'ARROW_UP':\n" +
                "                actions.send_keys(Keys.ARROW_UP)\n" +
                "            elif key == 'ARROW_DOWN':\n" +
                "                actions.send_keys(Keys.ARROW_DOWN)\n" +
                "            elif key == 'ARROW_LEFT':\n" +
                "                actions.send_keys(Keys.ARROW_LEFT)\n" +
                "            elif key == 'ARROW_RIGHT':\n" +
                "                actions.send_keys(Keys.ARROW_RIGHT)\n" +
                "            else:\n" +
                "                if re.match(r'F[1-9]|F1[0-2]', key):\n" +
                "                    actions.send_keys(getattr(Keys, key))\n" +
                "                elif len(key) == 1 and key.isalpha():\n" +
                "                    actions.send_keys(key)\n" +
                "                else:\n" +
                "                    print(f\"⚠️ 不支持的按键: {key}\")\n" +
                "        actions.key_up(Keys.CONTROL)\n" +
                "        actions.key_up(Keys.SHIFT)\n" +
                "        actions.key_up(Keys.ALT)\n";
    }

    private String generateHelperMethods() {
        return "def safe_wait(milliseconds):\n" +
                "    \"\"\"安全等待方法\"\"\"\n" +
                "    time.sleep(milliseconds / 1000.0)\n\n" +
                "def resolve_xpath_with_expression(xpath, context):\n" +
                "    \"\"\"解析XPath中的表达式\"\"\"\n" +
                "    if xpath is None:\n" +
                "        return None\n" +
                "    result = xpath\n" +
                "    pattern = r'\\{([^}]+)\\}'\n" +
                "    matches = re.finditer(pattern, xpath)\n" +
                "    \n" +
                "    for match in matches:\n" +
                "        full_match = match.group(0)\n" +
                "        expression = match.group(1)\n" +
                "        try:\n" +
                "            value = parse_expression(full_match, context)\n" +
                "            result = result.replace(full_match, str(value))\n" +
                "        except Exception as e:\n" +
                "            # 解析失败，保持原样\n" +
                "            pass\n" +
                "    return result\n\n" +
                "def resolve_value_with_expression(value, context):\n" +
                "    \"\"\"解析值中的表达式\"\"\"\n" +
                "    if value is None:\n" +
                "        return None\n" +
                "    result = value\n" +
                "    pattern = r'\\{([^}]+)\\}'\n" +
                "    matches = re.finditer(pattern, value)\n" +
                "    \n" +
                "    for match in matches:\n" +
                "        full_match = match.group(0)\n" +
                "        expression = match.group(1)\n" +
                "        try:\n" +
                "            expr_value = parse_expression(full_match, context)\n" +
                "            result = result.replace(full_match, str(expr_value))\n" +
                "        except Exception as e:\n" +
                "            # 解析失败，保持原样\n" +
                "            pass\n" +
                "    return result\n\n" +
                "def parse_expression(expression, context):\n" +
                "    \"\"\"解析表达式\"\"\"\n" +
                "    expr = expression.replace('{', '').replace('}', '').strip()\n" +
                "    \n" +
                "    if re.match(r'^\\d+$', expr):\n" +
                "        return int(expr)\n" +
                "    \n" +
                "    for var_name, var_value in context.items():\n" +
                "        if expr.startswith(var_name):\n" +
                "            base_value = int(var_value)\n" +
                "            operator_part = expr[len(var_name):].strip()\n" +
                "            \n" +
                "            if not operator_part:\n" +
                "                return base_value\n" +
                "            \n" +
                "            if re.match(r'[+\\-*/]\\s*\\d+', operator_part):\n" +
                "                operator = operator_part[0]\n" +
                "                number = int(operator_part[1:].strip())\n" +
                "                \n" +
                "                if operator == '+':\n" +
                "                    return base_value + number\n" +
                "                elif operator == '-':\n" +
                "                    return base_value - number\n" +
                "                elif operator == '*':\n" +
                "                    return base_value * number\n" +
                "                elif operator == '/':\n" +
                "                    return base_value // number\n" +
                "    \n" +
                "    return 1\n\n" +
                "def parse_key_sequence(input_str):\n" +
                "    \"\"\"解析键盘输入序列\"\"\"\n" +
                "    sequence = []\n" +
                "    current_text = []\n" +
                "    i = 0\n" +
                "    \n" +
                "    while i < len(input_str):\n" +
                "        c = input_str[i]\n" +
                "        \n" +
                "        if c == '{' and i + 1 < len(input_str):\n" +
                "            end_index = input_str.find('}', i)\n" +
                "            if end_index != -1:\n" +
                "                special_key = input_str[i+1:end_index].upper()\n" +
                "                if current_text:\n" +
                "                    sequence.append(''.join(current_text))\n" +
                "                    current_text = []\n" +
                "                \n" +
                "                if special_key == 'ENTER':\n" +
                "                    sequence.append(Keys.ENTER)\n" +
                "                elif special_key == 'TAB':\n" +
                "                    sequence.append(Keys.TAB)\n" +
                "                elif special_key in ['ESC', 'ESCAPE']:\n" +
                "                    sequence.append(Keys.ESCAPE)\n" +
                "                elif special_key == 'BACKSPACE':\n" +
                "                    sequence.append(Keys.BACKSPACE)\n" +
                "                elif special_key == 'DELETE':\n" +
                "                    sequence.append(Keys.DELETE)\n" +
                "                elif special_key == 'HOME':\n" +
                "                    sequence.append(Keys.HOME)\n" +
                "                elif special_key == 'END':\n" +
                "                    sequence.append(Keys.END)\n" +
                "                elif special_key == 'PAGEUP':\n" +
                "                    sequence.append(Keys.PAGE_UP)\n" +
                "                elif special_key == 'PAGEDOWN':\n" +
                "                    sequence.append(Keys.PAGE_DOWN)\n" +
                "                elif special_key in ['UP', 'ARROW_UP']:\n" +
                "                    sequence.append(Keys.ARROW_UP)\n" +
                "                elif special_key in ['DOWN', 'ARROW_DOWN']:\n" +
                "                    sequence.append(Keys.ARROW_DOWN)\n" +
                "                elif special_key in ['LEFT', 'ARROW_LEFT']:\n" +
                "                    sequence.append(Keys.ARROW_LEFT)\n" +
                "                elif special_key in ['RIGHT', 'ARROW_RIGHT']:\n" +
                "                    sequence.append(Keys.ARROW_RIGHT)\n" +
                "                elif special_key == 'CTRL':\n" +
                "                    sequence.append(Keys.CONTROL)\n" +
                "                elif special_key == 'SHIFT':\n" +
                "                    sequence.append(Keys.SHIFT)\n" +
                "                elif special_key == 'ALT':\n" +
                "                    sequence.append(Keys.ALT)\n" +
                "                else:\n" +
                "                    if re.match(r'F[1-9]|F1[0-2]', special_key):\n" +
                "                        sequence.append(getattr(Keys, special_key))\n" +
                "                    else:\n" +
                "                        current_text.append('{' + special_key + '}')\n" +
                "                i = end_index\n" +
                "            else:\n" +
                "                current_text.append(c)\n" +
                "        else:\n" +
                "            current_text.append(c)\n" +
                "        i += 1\n" +
                "    \n" +
                "    if current_text:\n" +
                "        sequence.append(''.join(current_text))\n" +
                "    \n" +
                "    return sequence\n\n";
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
        return "python";
    }

    @Override
    public String getFileExtension() {
        return ".py";
    }
}