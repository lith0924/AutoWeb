package org.example.auto_web.service.impl;

import org.example.auto_web.config.SeleniumConfig;
import org.example.auto_web.pojo.dto.CookieExportRequest;
import org.example.auto_web.pojo.dto.ExecuteRequest;
import org.example.auto_web.pojo.other.OperationStep;
import org.example.auto_web.service.SeleniumAutoWebService;
import org.example.auto_web.websocket.LogWebSocket;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class SeleniumAutoWebServiceImpl implements SeleniumAutoWebService {
    @Autowired
    private LogWebSocket logWebSocket;

    @Autowired
    private SeleniumConfig seleniumConfig;

    private static final Logger logger = LoggerFactory.getLogger(SeleniumAutoWebServiceImpl.class);
//    private static final String CHROME_DRIVER_PATH = "C:\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe";
//    private static final boolean HEADLESS_MODE = false;
//    private static final int TIMEOUT_SECONDS = 30;

    private String getChromeDriverPath() {
        return seleniumConfig.getChrome().getDriverPath();
    }

    private boolean isHeadlessMode() {
        return seleniumConfig.getChrome().isHeadlessMode();
    }

    private boolean isCloseBrowserAfterExec() {
        return seleniumConfig.getChrome().isCloseBrowserAfterExec();
    }

    private int getTimeoutSeconds() {
        return seleniumConfig.getTimeout().getSeconds();
    }

    private long getDefaultWaitAfterStep() {
        return seleniumConfig.getTimeout().getWaitAfterStep();
    }

    // 存储窗口句柄状态
    private static class WindowState {
        String mainWindowHandle;
        Set<String> previousHandles;

        WindowState(String mainHandle, Set<String> handles) {
            this.mainWindowHandle = mainHandle;
            this.previousHandles = handles;
        }
    }

    private void logInfo(String message) {
        try {
            logWebSocket.broadcastLog("[INFO] " + message);
            logger.info(message); // 同时记录到后端日志
        } catch (Exception e) {
            // 避免日志推送失败影响主流程
            logger.warn("推送日志到前端失败: {}", e.getMessage());
        }
    }

    private void logData(String message) {
        try {
            logWebSocket.broadcastLog("[DATA] " + message);
            logger.info("[DATA] " + message); // 同时记录到后端日志
        } catch (Exception e) {
            // 避免日志推送失败影响主流程
            logger.warn("推送数据日志到前端失败: {}", e.getMessage());
        }
    }

    private void logError(String message) {
        try {
            logWebSocket.broadcastLog("[ERROR] " + message);
            logger.info("[ERROR] " + message); // 同时记录到后端日志
        } catch (Exception e) {
            // 避免日志推送失败影响主流程
            logger.warn("推送数据日志到前端失败: {}", e.getMessage());
        }
    }

    @Override
    public void executeOperations(ExecuteRequest request) {
        WebDriver driver = null;

        try {
            logInfo("开始执行自动化操作");
//            System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
            System.setProperty("webdriver.chrome.driver", getChromeDriverPath());
            driver = createWebDriver();
            logInfo("ChromeDriver 初始化完成");

            if (request.getUrl() != null && !request.getUrl().isEmpty()) {
//                logInfo("正在访问: " + request.getUrl());
                driver.get(request.getUrl());
                logInfo("成功访问网址: " + request.getUrl());
            }

//            driver.manage().timeouts().implicitlyWait(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            driver.manage().timeouts().implicitlyWait(getTimeoutSeconds(), TimeUnit.SECONDS);

            // 初始化窗口状态
            WindowState windowState = new WindowState(
                    driver.getWindowHandle(),
                    driver.getWindowHandles()
            );

            logInfo("开始执行操作步骤，共 " + request.getSteps().size() + " 步");

            // 执行操作步骤
            executeSteps(driver, request.getSteps(), new HashMap<>(), windowState);

            logInfo("所有操作执行完成");

        } catch (Exception e) {
            String errorMsg = "执行自动化操作失败: " + e.getMessage();
            logError(errorMsg);
            logger.error("执行自动化操作失败", e);
            throw new RuntimeException("执行失败: " + e.getMessage(), e);
        } finally {
            if (driver != null && isCloseBrowserAfterExec()) {
                driver.quit();
                logInfo("浏览器已关闭");
            } else if (driver != null) {
                logInfo("保留浏览器窗口");
            }
        }
    }

    @Override
    public String exportCookie(CookieExportRequest request) {
        WebDriver driver = null;
        try {
            // 验证文件路径是否提供
            if (request.getFilePath() == null || request.getFilePath().trim().isEmpty()) {
                throw new IllegalArgumentException("文件路径不能为空");
            }

            logInfo("开始导出Cookie操作");
//            System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
            System.setProperty("webdriver.chrome.driver", getChromeDriverPath());
            driver = createWebDriver();
            logInfo("ChromeDriver 初始化完成");

            // 导航到目标网站
//            logInfo("正在导航到目标网站: " + request.getUrl());
            driver.get(request.getUrl());
            logInfo("已导航到目标网站: " + request.getUrl());

            // 等待用户操作
            if (request.getWaitSeconds() != null && request.getWaitSeconds() > 0) {
                logInfo("请手动操作网站，等待 " + request.getWaitSeconds() + " 秒后自动导出Cookie...");
                Thread.sleep(request.getWaitSeconds() * 1000L);
            }

            // 获取所有Cookie
            logInfo("正在获取Cookie...");
            Set<Cookie> cookies = driver.manage().getCookies();
            List<Map<String, Object>> cookieList = new ArrayList<>();

            for (Cookie cookie : cookies) {
                Map<String, Object> cookieMap = new HashMap<>();
                cookieMap.put("name", cookie.getName());
                cookieMap.put("value", cookie.getValue());
                cookieMap.put("domain", cookie.getDomain());
                cookieMap.put("path", cookie.getPath());
                cookieMap.put("expiry", cookie.getExpiry());
                cookieMap.put("secure", cookie.isSecure());
                cookieMap.put("httpOnly", cookie.isHttpOnly());
                cookieList.add(cookieMap);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String cookieJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cookieList);

            // 保存到文件 - 必须提供文件路径
            String filePath = request.getFilePath();

            Files.write(Paths.get(filePath), cookieJson.getBytes(StandardCharsets.UTF_8));

            String successMsg = "成功导出 " + cookies.size() + " 个Cookie到文件: " + filePath;
            logInfo(successMsg);
            return "Cookie已导出到: " + filePath;

        } catch (Exception e) {
            String errorMsg = "导出Cookie失败: " + e.getMessage();
            logError(errorMsg);
            logger.error("导出Cookie失败", e);
            throw new RuntimeException("导出Cookie失败: " + e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
                logInfo("浏览器已关闭");
            }
        }
    }

    private void executeSteps(WebDriver driver, List<OperationStep> steps,
                              Map<String, Object> context, WindowState windowState) {
        if (steps == null || steps.isEmpty()) {
            logInfo("没有要执行的操作步骤");
            return;
        }

        for (int i = 0; i < steps.size(); i++) {
            OperationStep step = steps.get(i);
            try {
                // 打印步骤备注
                if (step.getRemark() != null && !step.getRemark().isEmpty()) {
                    logInfo("步骤备注: " + step.getRemark());
                }
                logInfo("执行第 " + (i + 1) + " 步: " + step.getType());

                // 在执行步骤前检查并切换到最新窗口
                autoSwitchToNewWindow(driver, windowState);

                executeSingleStep(driver, step, context, windowState);

                // 在执行步骤后再次检查窗口状态，确保及时切换到新窗口
                checkAndUpdateWindowState(driver, windowState);

                logInfo("第 " + (i + 1) + " 步执行完成");

            } catch (Exception e) {
                String errorMsg = "执行步骤失败: " + step.getType() + " - " + e.getMessage();
                logError(errorMsg);
                logger.error("执行步骤失败: {}", step.getType(), e);
                throw new RuntimeException("步骤执行失败: " + step.getType(), e);
            }
        }
    }

    private void executeSingleStep(WebDriver driver, OperationStep step,
                                   Map<String, Object> context, WindowState windowState) {
        // 操作前等待
        performWait(step.getWaitBeforeMs(), "操作前");

        // 执行具体操作
        switch (step.getType()) {
            case CLICK:
                executeClick(driver, step, context);
                break;
            case INPUT:
                executeInput(driver, step, context);
                break;
            case GET_TEXT:
                executeGetText(driver, step, context);
                break;
            case LOOP_GET_TEXT:
                executeLoopGetText(driver, step, context);
                break;
            case LOOP_CLICK:
                executeLoopClick(driver, step, context);
                break;
            case LOOP_INPUT:
                executeLoopInput(driver, step, context);
                break;
            case LOOP_TASK:
                executeLoopTask(driver, step, context, windowState);
                break;
            case DYNAMIC_LOOP:
                executeDynamicLoop(driver, step, context, windowState);
                break;
            case WAIT:
                executeWait(step);
                break;
            case NAVIGATE:
                executeNavigate(driver, step);
                break;
            case SWITCH_IFRAME:
                executeSwitchIframe(driver, step, context);
                break;
            case GET_CURRENT_URL:
                executeGetCurrentUrl(driver, step, context);
                break;
            case PRESS_KEYS:
                executePressKeys(driver, step, context);
                break;
            case KEYBOARD_INPUT:
                executeKeyboardInput(driver, step, context);
                break;
            case GO_BACK:
                executeGoBack(driver);
                break;
            case HANDLE_ALERT:
                executeHandleAlert(driver, step);
                break;
            case CLOSE_TAB:
                executeCloseTab(driver, windowState);
                break;
            case IMPORT_COOKIE:
                executeImportCookie(driver, step);
                break;
            default:
                throw new IllegalArgumentException("不支持的操作类型: " + step.getType());
        }

        // 操作后等待
        performWait(step.getWaitAfterMs(), "操作后");
    }

    /**
     * 解析表达式，支持简单的数学运算
     * 支持：{i+1}, {index*2}, {n-3}, {page/2} 等
     */
    private int parseExpression(String expression, Map<String, Object> context) {
        // 移除花括号
        String expr = expression.replace("{", "").replace("}", "").trim();

        // 检查是否是简单的数字
        if (expr.matches("\\d+")) {
            return Integer.parseInt(expr);
        }

        // 检查是否包含变量和运算符
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String varName = entry.getKey();
            if (expr.startsWith(varName)) {
                int baseValue = Integer.parseInt(entry.getValue().toString());
                String operatorPart = expr.substring(varName.length()).trim();

                if (operatorPart.isEmpty()) {
                    return baseValue;
                }

                // 解析运算符和数值
                if (operatorPart.matches("[+\\-*/]\\s*\\d+")) {
                    char operator = operatorPart.charAt(0);
                    int number = Integer.parseInt(operatorPart.substring(1).trim());

                    switch (operator) {
                        case '+':
                            return baseValue + number;
                        case '-':
                            return baseValue - number;
                        case '*':
                            return baseValue * number;
                        case '/':
                            return baseValue / number;
                        default:
                            throw new IllegalArgumentException("不支持的运算符: " + operator);
                    }
                }
            }
        }

        // 如果没有匹配的变量，尝试直接计算
        try {
            // 简单的表达式计算（仅支持单个变量）
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                String varName = entry.getKey();
                if (expr.contains(varName)) {
                    int value = Integer.parseInt(entry.getValue().toString());
                    String calcExpr = expr.replace(varName, String.valueOf(value));
                    // 简单的表达式求值
                    return evaluateSimpleExpression(calcExpr);
                }
            }
        } catch (Exception e) {
            logInfo("表达式解析失败: " + expression + ", 使用默认值1");
        }

        return 1;
    }

    /**
     * 简单表达式求值
     */
    private int evaluateSimpleExpression(String expression) {
        try {
            // 使用JavaScript引擎进行简单计算
            javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
            javax.script.ScriptEngine engine = manager.getEngineByName("JavaScript");
            Object result = engine.eval(expression);
            return Integer.parseInt(result.toString());
        } catch (Exception e) {
            logError("表达式计算失败: " + expression + ", 使用默认值1");
            return 1;
        }
    }

    /**
     * 解析XPath中的表达式 - 通用方法
     */
    private String resolveXpathWithExpression(String xpath, Map<String, Object> context) {
        if (xpath == null) return null;

        String result = xpath;
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(xpath);

        while (matcher.find()) {
            String fullMatch = matcher.group(0); // 完整的 {i+1}
            String expression = matcher.group(1); // 表达式部分 i+1

            try {
                int value = parseExpression(fullMatch, context);
                result = result.replace(fullMatch, String.valueOf(value));
                logInfo("表达式解析成功: " + fullMatch + " -> " + value);
            } catch (Exception e) {
                logError("表达式解析失败: " + fullMatch + ", 使用原始文本");
            }
        }

        return result;
    }

    /**
     * 解析值中的表达式 - 通用方法
     */
    private String resolveValueWithExpression(String value, Map<String, Object> context) {
        if (value == null) return null;

        String result = value;
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(value);

        while (matcher.find()) {
            String fullMatch = matcher.group(0); // 完整的 {i+1}
            String expression = matcher.group(1); // 表达式部分 i+1

            try {
                int exprValue = parseExpression(fullMatch, context);
                result = result.replace(fullMatch, String.valueOf(exprValue));
                logInfo("值表达式解析成功: " + fullMatch + " -> " + exprValue);
            } catch (Exception e) {
                logError("值表达式解析失败: " + fullMatch + ", 使用原始文本");
            }
        }

        return result;
    }


    /**
     * 导入Cookie（从文件）
     */
    private void executeImportCookie(WebDriver driver, OperationStep step) {
        if (step.getFilePath() == null || step.getFilePath().isEmpty()) {
            logInfo("没有提供Cookie文件路径，跳过导入");
            return;
        }

        try {
            logInfo("开始从文件导入Cookie: " + step.getFilePath());

            // 读取Cookie文件
            String cookieJson = new String(Files.readAllBytes(Paths.get(step.getFilePath())), StandardCharsets.UTF_8);

            // 解析JSON格式的Cookie数据
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> cookies = objectMapper.readValue(cookieJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            logInfo("解析到 " + cookies.size() + " 个Cookie");

            // 导入Cookie
            int importedCount = 0;
            for (Map<String, Object> cookieMap : cookies) {
                try {
                    Cookie.Builder cookieBuilder = new Cookie.Builder(
                            cookieMap.get("name").toString(),
                            cookieMap.get("value").toString()
                    );

                    cookieBuilder.domain(cookieMap.get("domain").toString());

                    if (cookieMap.containsKey("path")) {
                        cookieBuilder.path(cookieMap.get("path").toString());
                    } else {
                        cookieBuilder.path("/");
                    }

                    if (cookieMap.containsKey("expiry") && cookieMap.get("expiry") != null) {
                        String expiryStr = cookieMap.get("expiry").toString();
                        try {
                            if (expiryStr.contains("T") && expiryStr.contains("Z")) {
                                Date expiryDate = Date.from(java.time.Instant.parse(expiryStr));
                                cookieBuilder.expiresOn(expiryDate);
                            } else {
                                long expiryTimestamp = Long.parseLong(expiryStr);
                                cookieBuilder.expiresOn(new Date(expiryTimestamp));
                            }
                        } catch (Exception e) {
                            logError("解析Cookie过期时间失败: " + expiryStr);
                        }
                    }

                    if (cookieMap.containsKey("secure")) {
                        cookieBuilder.isSecure(Boolean.parseBoolean(cookieMap.get("secure").toString()));
                    }

                    driver.manage().addCookie(cookieBuilder.build());
                    importedCount++;
                    logInfo("导入Cookie: " + cookieMap.get("name"));
                } catch (Exception e) {
                    logError("导入单个Cookie失败: " + e.getMessage());
                }
            }

            logInfo("成功导入 " + importedCount + " 个Cookie");

            // 刷新页面使Cookie生效
            logInfo("正在刷新页面使Cookie生效...");
            driver.navigate().refresh();
            logInfo("已刷新页面使Cookie生效");

        } catch (Exception e) {
            logError("导入Cookie失败: " + e.getMessage());
            throw new RuntimeException("导入Cookie失败", e);
        }
    }

    /**
     * 关闭当前标签页
     */
    private void executeCloseTab(WebDriver driver, WindowState windowState) {
        String currentHandle = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();

        if (handles.size() <= 1) {
            logInfo("只有一个标签页，无法关闭");
            return;
        }

        logInfo("准备关闭当前标签页");

        // 关闭当前标签页
        driver.close();

        // 切换到剩余的第一个标签页
        handles = driver.getWindowHandles();
        if (!handles.isEmpty()) {
            String newHandle = handles.iterator().next();
            driver.switchTo().window(newHandle);
            logInfo("已关闭标签页，切换到新标签页");
            logInfo("当前URL: " + driver.getCurrentUrl());
        }

        // 更新窗口状态
        windowState.previousHandles = handles;
        if (!handles.isEmpty()) {
            windowState.mainWindowHandle = handles.iterator().next();
        }
    }

    /**
     * 遍历获取内容并写入文件 - 支持表达式
     */
    private void executeLoopGetText(WebDriver driver, OperationStep step, Map<String, Object> context) {
        Integer startIndex = step.getStartIndex() != null ? step.getStartIndex() : 1;
        Integer endIndex = step.getEndIndex() != null ? step.getEndIndex() : 1;
        Integer increment = step.getIncrement() != null ? step.getIncrement() : 1;
        String filePath = step.getFilePath() != null ? step.getFilePath() :
                "text_output_" + System.currentTimeMillis() + ".txt";

        logInfo("准备遍历获取文本并写入文件");
        logInfo("   - 索引范围: " + startIndex + " - " + endIndex + " (增量: " + increment + ")");
        logInfo("   - 输出文件: " + filePath);

        List<String> textList = new ArrayList<>();

        for (int i = startIndex; i <= endIndex; i += increment) {
            // 设置上下文变量
            context.put("i", i);
            context.put("index", i);
            context.put("current", i);

            // 使用表达式解析XPath
            String resolvedXpath = resolveXpathWithExpression(step.getXpath(), context);

            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(resolvedXpath)));

                String text = element.getText().trim();
                textList.add(text);
                // 使用数据日志记录获取的文本内容
                logData("获取文本 [" + i + "]: '" + text + "'");

                long waitTime = step.getWaitAfterMs() != null ? step.getWaitAfterMs() : 500;
                Thread.sleep(waitTime);
            } catch (Exception e) {
                logError("获取文本失败，索引: " + i);
                textList.add(""); // 添加空行保持顺序
            }
        }

        // 写入文件
        try {
            Files.write(Paths.get(filePath), textList, StandardCharsets.UTF_8);
            logInfo("💾 成功将 " + textList.size() + " 条文本写入文件: " + filePath);
        } catch (Exception e) {
            logError("写入文件失败: " + filePath);
            throw new RuntimeException("写入文件失败: " + filePath, e);
        }

        // 清理上下文变量
        context.remove("i");
        context.remove("index");
        context.remove("current");
    }

    /**
     * 返回上一个页面
     */
    private void executeGoBack(WebDriver driver) {
        String currentUrl = driver.getCurrentUrl();
        driver.navigate().back();
        String newUrl = driver.getCurrentUrl();
        logInfo("返回上一页: " + currentUrl + " -> " + newUrl);
    }

    /**
     * 处理弹窗
     */
    private void executeHandleAlert(WebDriver driver, OperationStep step) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());

            if (step.getAlertText() != null && !step.getAlertText().isEmpty()) {
                alert.sendKeys(step.getAlertText());
                logInfo("在弹窗中输入文本: " + step.getAlertText());
            }

            if (step.getAcceptAlert() != null) {
                if (step.getAcceptAlert()) {
                    alert.accept();
                    logInfo("接受弹窗");
                } else {
                    alert.dismiss();
                    logInfo("取消弹窗");
                }
            }
        } catch (TimeoutException e) {
            logError("在指定时间内未检测到弹窗");
        } catch (Exception e) {
            logError("处理弹窗失败: " + e.getMessage());
            throw new RuntimeException("处理弹窗失败", e);
        }
    }

    /**
     * 动态循环 - 支持表达式
     */
    private void executeDynamicLoop(WebDriver driver, OperationStep step,
                                    Map<String, Object> context, WindowState windowState) {
        String loopVar = step.getValue() != null ? step.getValue() : "dynamic_index";
        Integer iterations = step.getIterations() != null ? step.getIterations() : 1;
        Integer increment = step.getIncrement() != null ? step.getIncrement() : 1;

        for (int i = 0; i < iterations; i += increment) {
            context.put(loopVar, i);
            context.put("i", i);
            context.put("index", i);
            logInfo("开始动态循环迭代: " + (i + 1) + "/" + iterations + " (增量: " + increment + ")");

            if (step.getSubSteps() != null) {
                // 为子步骤创建新的窗口状态副本
                WindowState subWindowState = new WindowState(
                        windowState.mainWindowHandle,
                        new HashSet<>(driver.getWindowHandles())
                );
                executeSteps(driver, step.getSubSteps(), context, subWindowState);
            }

            // 每次循环后等待一下，避免操作过快
            performWait(step.getWaitAfterMs() != null ? step.getWaitAfterMs() : 1000L, "动态循环后");
        }

        // 清理上下文变量
        context.remove(loopVar);
        context.remove("i");
        context.remove("index");
    }

    /**
     * 循环任务 - 支持表达式
     */
    private void executeLoopTask(WebDriver driver, OperationStep step,
                                 Map<String, Object> context, WindowState windowState) {
        String loopVar = step.getValue() != null ? step.getValue() : "i";
        Integer iterations = step.getIterations() != null ? step.getIterations() : 1;
        Integer increment = step.getIncrement() != null ? step.getIncrement() : 1;

        for (int i = 0; i < iterations; i += increment) {
            context.put(loopVar, i);
            context.put("i", i);
            context.put("index", i);
            logInfo("开始循环任务迭代: " + (i + 1) + "/" + iterations + " (增量: " + increment + ")");

            if (step.getSubSteps() != null) {
                // 为子步骤创建新的窗口状态副本
                WindowState subWindowState = new WindowState(
                        windowState.mainWindowHandle,
                        new HashSet<>(driver.getWindowHandles())
                );
                executeSteps(driver, step.getSubSteps(), context, subWindowState);
            }
        }

        // 清理上下文变量
        context.remove(loopVar);
        context.remove("i");
        context.remove("index");
    }

    /**
     * 获取当前URL
     */
    private void executeGetCurrentUrl(WebDriver driver, OperationStep step, Map<String, Object> context) {
        String currentUrl = driver.getCurrentUrl();
        String key = step.getValue() != null ? step.getValue() : "current_url";

        context.put(key, currentUrl);
        logData("获取当前URL: " + currentUrl);
        logInfo("已保存到上下文: " + key + " = " + currentUrl);
    }

    /**
     * 切换iframe - 支持表达式
     */
    private void executeSwitchIframe(WebDriver driver, OperationStep step, Map<String, Object> context) {
        String resolvedXpath = resolveXpathWithExpression(step.getXpath(), context);

        logInfo("准备切换iframe");

        try {
            if ("default".equalsIgnoreCase(resolvedXpath)) {
                // 切换回默认内容
                driver.switchTo().defaultContent();
                logInfo("已切换回默认内容");
            } else if ("parent".equalsIgnoreCase(resolvedXpath)) {
                // 切换回父级iframe
                driver.switchTo().parentFrame();
                logInfo("已切换回父级iframe");
            } else {
                // 切换到指定iframe
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement iframeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(resolvedXpath)));
                driver.switchTo().frame(iframeElement);
                logInfo("已切换到iframe");
            }
        } catch (Exception e) {
            logError("切换iframe失败: " + e.getMessage());
            throw new RuntimeException("切换iframe失败: " + resolvedXpath, e);
        }
    }

    /**
     * 模拟按键操作
     */
    private void executePressKeys(WebDriver driver, OperationStep step, Map<String, Object> context) {
        String resolvedValue = resolveValueWithExpression(step.getValue(), context);

        logInfo("准备模拟按键操作: " + resolvedValue);

        try {
            Actions actions = new Actions(driver);

            // 解析按键序列（支持组合键，用+连接）
            String[] keySequence = resolvedValue.split("\\+");
            for (String key : keySequence) {
                key = key.trim().toUpperCase();
                switch (key) {
                    case "CTRL":
                        actions.keyDown(Keys.CONTROL);
                        break;
                    case "SHIFT":
                        actions.keyDown(Keys.SHIFT);
                        break;
                    case "ALT":
                        actions.keyDown(Keys.ALT);
                        break;
                    case "ENTER":
                        actions.sendKeys(Keys.ENTER);
                        break;
                    case "TAB":
                        actions.sendKeys(Keys.TAB);
                        break;
                    case "ESC":
                        actions.sendKeys(Keys.ESCAPE);
                        break;
                    case "BACKSPACE":
                        actions.sendKeys(Keys.BACK_SPACE);
                        break;
                    case "DELETE":
                        actions.sendKeys(Keys.DELETE);
                        break;
                    case "HOME":
                        actions.sendKeys(Keys.HOME);
                        break;
                    case "END":
                        actions.sendKeys(Keys.END);
                        break;
                    case "PAGEUP":
                        actions.sendKeys(Keys.PAGE_UP);
                        break;
                    case "PAGEDOWN":
                        actions.sendKeys(Keys.PAGE_DOWN);
                        break;
                    case "ARROW_UP":
                        actions.sendKeys(Keys.ARROW_UP);
                        break;
                    case "ARROW_DOWN":
                        actions.sendKeys(Keys.ARROW_DOWN);
                        break;
                    case "ARROW_LEFT":
                        actions.sendKeys(Keys.ARROW_LEFT);
                        break;
                    case "ARROW_RIGHT":
                        actions.sendKeys(Keys.ARROW_RIGHT);
                        break;
                    case "F1": case "F2": case "F3": case "F4": case "F5": case "F6":
                    case "F7": case "F8": case "F9": case "F10": case "F11": case "F12":
                        actions.sendKeys(Keys.valueOf("F" + key.substring(1)));
                        break;
                    case "A": case "B": case "C": case "D": case "E": case "F": case "G":
                    case "H": case "I": case "J": case "K": case "L": case "M": case "N":
                    case "O": case "P": case "Q": case "R": case "S": case "T": case "U":
                    case "V": case "W": case "X": case "Y": case "Z":
                        // 将大写字母转换为小写，因为 sendKeys 需要小写字母
                        actions.sendKeys(key.toLowerCase());
                        break;
                    case "0": case "1": case "2": case "3": case "4":
                    case "5": case "6": case "7": case "8": case "9":
                        // 添加数字键支持
                        actions.sendKeys(key);
                        break;
                    case "SPACE":
                        actions.sendKeys(Keys.SPACE);
                        break;
                    default:
                        logInfo("不支持的按键: " + key);
                }
            }

            // 释放所有修饰键
            actions.keyUp(Keys.CONTROL).keyUp(Keys.SHIFT).keyUp(Keys.ALT);
            actions.build().perform();

            logInfo("模拟按键操作完成: " + resolvedValue);

        } catch (Exception e) {
            logError("模拟按键操作失败: " + e.getMessage());
            throw new RuntimeException("模拟按键操作失败: " + resolvedValue, e);
        }
    }

    /**
     * 模拟键盘输入 - 支持表达式
     */
    private void executeKeyboardInput(WebDriver driver, OperationStep step, Map<String, Object> context) {
        String resolvedValue = resolveValueWithExpression(step.getValue(), context);
        String resolvedXpath = step.getXpath() != null ? resolveXpathWithExpression(step.getXpath(), context) : null;

        logInfo("准备模拟键盘输入: " + resolvedValue);

        try {
            Actions actions = new Actions(driver);

            // 如果有指定元素，先点击元素获得焦点
            if (resolvedXpath != null) {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(resolvedXpath)));
                element.click();
                logInfo("已点击目标元素获得焦点");
            }

            // 解析并执行键盘输入
            actions.sendKeys(parseKeySequence(resolvedValue)).perform();

            logInfo("模拟键盘输入完成: " + resolvedValue);

        } catch (Exception e) {
            logError("模拟键盘输入失败: " + e.getMessage());
            throw new RuntimeException("模拟键盘输入失败: " + resolvedValue, e);
        }
    }

    /**
     * 解析键盘输入序列
     */
    private CharSequence[] parseKeySequence(String input) {
        List<CharSequence> sequence = new ArrayList<>();
        StringBuilder currentText = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '{' && i + 1 < input.length()) {
                // 处理特殊键
                int endIndex = input.indexOf('}', i);
                if (endIndex != -1) {
                    String specialKey = input.substring(i + 1, endIndex).toUpperCase();
                    if (currentText.length() > 0) {
                        sequence.add(currentText.toString());
                        currentText.setLength(0);
                    }

                    switch (specialKey) {
                        case "ENTER":
                            sequence.add(Keys.ENTER);
                            break;
                        case "TAB":
                            sequence.add(Keys.TAB);
                            break;
                        case "ESC":
                        case "ESCAPE":
                            sequence.add(Keys.ESCAPE);
                            break;
                        case "BACKSPACE":
                            sequence.add(Keys.BACK_SPACE);
                            break;
                        case "DELETE":
                            sequence.add(Keys.DELETE);
                            break;
                        case "HOME":
                            sequence.add(Keys.HOME);
                            break;
                        case "END":
                            sequence.add(Keys.END);
                            break;
                        case "PAGEUP":
                            sequence.add(Keys.PAGE_UP);
                            break;
                        case "PAGEDOWN":
                            sequence.add(Keys.PAGE_DOWN);
                            break;
                        case "UP":
                        case "ARROW_UP":
                            sequence.add(Keys.ARROW_UP);
                            break;
                        case "DOWN":
                        case "ARROW_DOWN":
                            sequence.add(Keys.ARROW_DOWN);
                            break;
                        case "LEFT":
                        case "ARROW_LEFT":
                            sequence.add(Keys.ARROW_LEFT);
                            break;
                        case "RIGHT":
                        case "ARROW_RIGHT":
                            sequence.add(Keys.ARROW_RIGHT);
                            break;
                        case "CTRL":
                            sequence.add(Keys.CONTROL);
                            break;
                        case "SHIFT":
                            sequence.add(Keys.SHIFT);
                            break;
                        case "ALT":
                            sequence.add(Keys.ALT);
                            break;
                        default:
                            // 如果是F1-F12
                            if (specialKey.matches("F[1-9]|F1[0-2]")) {
                                sequence.add(Keys.valueOf(specialKey));
                            } else {
                                logInfo("不支持的特殊键: " + specialKey);
                                currentText.append("{" + specialKey + "}");
                            }
                    }

                    i = endIndex; // 跳过已处理的部分
                } else {
                    currentText.append(c);
                }
            } else {
                currentText.append(c);
            }
        }

        // 添加剩余文本
        if (currentText.length() > 0) {
            sequence.add(currentText.toString());
        }

        return sequence.toArray(new CharSequence[0]);
    }


    /**
     * 点击操作 - 支持表达式
     */
    private void executeClick(WebDriver driver, OperationStep step, Map<String, Object> context) {
        String resolvedXpath = resolveXpathWithExpression(step.getXpath(), context);

        logInfo("准备点击操作");
        logInfo("当前URL: " + driver.getCurrentUrl());

        try {
            // 增加显式等待，确保元素存在
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(resolvedXpath)));

            element.click();
            logInfo("点击元素成功");

            // 点击后等待页面可能的变化
            Thread.sleep(1000);

        } catch (TimeoutException e) {
            logError("元素查找超时");
            logError("可能的原因:");
            logError("  - XPath 不正确");
            logError("  - 元素尚未加载完成");
            logError("  - 当前页面URL: " + driver.getCurrentUrl());
            throw new RuntimeException("元素查找超时: " + resolvedXpath, e);
        } catch (Exception e) {
            logError("点击元素失败: " + e.getMessage());
            throw new RuntimeException("点击元素失败: " + resolvedXpath, e);
        }
    }

    /**
     * 输入操作 - 支持表达式
     */
    private void executeInput(WebDriver driver, OperationStep step, Map<String, Object> context) {
        String resolvedXpath = resolveXpathWithExpression(step.getXpath(), context);
        String resolvedValue = resolveValueWithExpression(step.getValue(), context);

        logInfo("准备输入操作");
        logInfo("  - 输入值: " + resolvedValue);
        logInfo("  - 当前URL: " + driver.getCurrentUrl());

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(resolvedXpath)));

            element.clear();
            element.sendKeys(resolvedValue);
            logInfo("输入内容成功: '" + resolvedValue + "'");

        } catch (Exception e) {
            logError("输入内容失败: " + e.getMessage());
            throw new RuntimeException("输入内容失败: " + resolvedXpath, e);
        }
    }

    /**
     * 获取文本操作 - 支持表达式
     */
    private void executeGetText(WebDriver driver, OperationStep step, Map<String, Object> context) {
        String resolvedXpath = resolveXpathWithExpression(step.getXpath(), context);

        logInfo("准备获取文本操作");
        logInfo("  - 当前URL: " + driver.getCurrentUrl());

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(resolvedXpath)));

            String text = element.getText();
            // 使用数据日志记录获取的文本内容
            logData("获取文本: '" + text + "'");
            logInfo("获取文本成功");

        } catch (Exception e) {
            logError("获取文本失败: " + e.getMessage());
            throw new RuntimeException("获取文本失败: " + resolvedXpath, e);
        }
    }

    /**
     * 遍历点击 - 支持表达式
     */
    private void executeLoopClick(WebDriver driver, OperationStep step, Map<String, Object> context) {
        Integer startIndex = step.getStartIndex() != null ? step.getStartIndex() : 1;
        Integer endIndex = step.getEndIndex() != null ? step.getEndIndex() : 1;
        Integer increment = step.getIncrement() != null ? step.getIncrement() : 1;

        logInfo("准备遍历点击操作");
        logInfo("  - 索引范围: " + startIndex + " - " + endIndex + " (增量: " + increment + ")");

        for (int i = startIndex; i <= endIndex; i += increment) {
            // 设置上下文变量
            context.put("i", i);
            context.put("index", i);
            context.put("current", i);

            // 使用表达式解析XPath
            String resolvedXpath = resolveXpathWithExpression(step.getXpath(), context);

            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(resolvedXpath)));

                element.click();
                logInfo("遍历点击成功: 索引: " + i);

                long waitTime = step.getWaitAfterMs() != null ? step.getWaitAfterMs() : 500;
                Thread.sleep(waitTime);
            } catch (Exception e) {
                logError("遍历点击失败，索引: " + i);
            }
        }

        // 清理上下文变量
        context.remove("i");
        context.remove("index");
        context.remove("current");
    }

    /**
     * 遍历输入 - 支持表达式
     */
    private void executeLoopInput(WebDriver driver, OperationStep step, Map<String, Object> context) {
        Integer startIndex = step.getStartIndex() != null ? step.getStartIndex() : 1;
        Integer endIndex = step.getEndIndex() != null ? step.getEndIndex() : 1;
        Integer increment = step.getIncrement() != null ? step.getIncrement() : 1;

        logInfo("准备遍历输入操作");
        logInfo("  - 索引范围: " + startIndex + " - " + endIndex + " (增量: " + increment + ")");

        for (int i = startIndex; i <= endIndex; i += increment) {
            // 设置上下文变量
            context.put("i", i);
            context.put("index", i);
            context.put("current", i);

            // 使用表达式解析XPath和值
            String resolvedXpath = resolveXpathWithExpression(step.getXpath(), context);
            String resolvedValue = resolveValueWithExpression(step.getValue(), context);

            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(resolvedXpath)));

                element.clear();
                element.sendKeys(resolvedValue);
                logInfo("遍历输入成功: 索引: " + i);
                logInfo("   - 输入的值: " + resolvedValue);

                long waitTime = step.getWaitAfterMs() != null ? step.getWaitAfterMs() : 500;
                Thread.sleep(waitTime);
            } catch (Exception e) {
                logError("遍历输入失败，索引: " + i);
            }
        }

        // 清理上下文变量
        context.remove("i");
        context.remove("index");
        context.remove("current");
    }

    private void executeWait(OperationStep step) {
        long waitTime = 1000L;
        if (step.getParameters() != null && step.getParameters().containsKey("milliseconds")) {
            waitTime = Long.parseLong(step.getParameters().get("milliseconds").toString());
        } else if (step.getWaitBeforeMs() != null) {
            waitTime = step.getWaitBeforeMs();
        } else if (step.getWaitAfterMs() != null) {
            waitTime = step.getWaitAfterMs();
        }

        try {
            logInfo("等待 " + waitTime + " 毫秒");
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("等待被中断", e);
        }
    }

    private void executeNavigate(WebDriver driver, OperationStep step) {
        if (step.getValue() != null) {
            String currentUrl = driver.getCurrentUrl();
            driver.get(step.getValue());
            String newUrl = driver.getCurrentUrl();
            logInfo("页面跳转: " + currentUrl + " -> " + newUrl);
        }
    }


    /**
     * 自动切换到最新窗口
     */
    private void autoSwitchToNewWindow(WebDriver driver, WindowState windowState) {
        Set<String> currentHandles = driver.getWindowHandles();
        String currentHandle = driver.getWindowHandle();

        logInfo("窗口检查 - 总数: " + currentHandles.size());

        // 如果有多个窗口，且当前不是最后一个窗口，就切换到最后一个窗口
        if (currentHandles.size() > 1) {
            // 获取最后一个窗口句柄（通常是最新打开的）
            String lastHandle = getLastWindowHandle(currentHandles);

            if (!lastHandle.equals(currentHandle)) {
                String currentUrl = driver.getCurrentUrl();
                driver.switchTo().window(lastHandle);
                String newUrl = driver.getCurrentUrl();

                logInfo("切换到最新窗口");
                logInfo("   - 从: " + currentUrl);
                logInfo("   - 到: " + newUrl);
            } else {
                logInfo("已在最新窗口: " + driver.getCurrentUrl());
            }
        } else {
            logInfo("只有一个窗口: " + driver.getCurrentUrl());
        }

        // 更新窗口状态
        windowState.previousHandles = currentHandles;
    }

    /**
     * 检查并更新窗口状态
     */
    private void checkAndUpdateWindowState(WebDriver driver, WindowState windowState) {
        Set<String> currentHandles = driver.getWindowHandles();

        // 如果窗口数量增加了，说明操作可能打开了新窗口
        if (currentHandles.size() > windowState.previousHandles.size()) {
            logInfo("检测到操作后窗口数量变化: " + windowState.previousHandles.size() + " -> " + currentHandles.size());

            // 立即切换到最新窗口
            autoSwitchToNewWindow(driver, windowState);
        }

        // 更新窗口状态
        windowState.previousHandles = currentHandles;
    }

    /**
     * 获取最后一个窗口句柄
     */
    private String getLastWindowHandle(Set<String> handles) {
        String lastHandle = null;
        for (String handle : handles) {
            lastHandle = handle;
        }
        return lastHandle;
    }

    /**
     * 等待方法
     */
    private void performWait(Long waitMs, String type) {
        if (waitMs != null && waitMs > 0) {
            try {
                logInfo( type + "等待 " + waitMs + " 毫秒");
                Thread.sleep(waitMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("等待被中断", e);
            }
        }
    }

    /**
     * 创建WebDriver实例
     */
    private WebDriver createWebDriver() {
        ChromeOptions options = new ChromeOptions();
        if (isHeadlessMode()) {
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
        }
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-extensions");
        options.addArguments("--start-maximized");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        logInfo("创建ChromeDriver实例，无头模式: " + isHeadlessMode());
        return new ChromeDriver(options);
    }
}