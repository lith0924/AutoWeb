package org.example.auto_web.pojo.enums;

public enum OperationType {
    CLICK,           // 点击
    INPUT,           // 输入
    GET_TEXT,        // 获取内容
    LOOP_GET_TEXT,   // 遍历获取内容
    LOOP_CLICK,      // 遍历点击
    LOOP_INPUT,      // 遍历输入
    LOOP_TASK,       // 循环任务
    DYNAMIC_LOOP,    // 动态循环
    WAIT,            // 等待
    NAVIGATE,        // 页面跳转
    SWITCH_IFRAME,   // 切换iframe
    GET_CURRENT_URL, // 获取当前URL
    PRESS_KEYS,      // 模拟按键
    KEYBOARD_INPUT,  // 模拟键盘输入
    GO_BACK,         // 返回上一页
    HANDLE_ALERT,    // 处理弹窗
    CLOSE_TAB,       // 关闭当前标签页
    IMPORT_COOKIE    // 导入Cookie
}