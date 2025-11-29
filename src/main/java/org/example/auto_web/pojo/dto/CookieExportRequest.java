package org.example.auto_web.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CookieExportRequest {
    private String url;          // 目标网址
    private Integer waitSeconds; // 等待时间
    private String filePath;     // 导出文件路径
}