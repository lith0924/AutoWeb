package org.example.auto_web.service;

import org.example.auto_web.pojo.dto.CookieExportRequest;
import org.example.auto_web.pojo.dto.ExecuteRequest;

import java.util.Map;

public interface SeleniumAutoWebService {
    void executeOperations(ExecuteRequest request);
    String exportCookie(CookieExportRequest request);
}