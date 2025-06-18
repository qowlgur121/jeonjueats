package com.jeonjueats.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Bae-Jihyeok, qowlgur121@gmail.com
 * @date 2025-06-18
 * @description Digital Asset Links를 위한 .well-known 엔드포인트 제공
 *             TWA(Trusted Web Activity) 앱이 웹사이트와 연결되도록 설정
 */
@RestController
@RequestMapping("/.well-known")
public class WellKnownController {

    /**
     * TWA Digital Asset Links 파일 제공
     * Android 앱이 이 웹사이트의 URL을 처리할 수 있도록 허용하는 설정
     * 
     * @return assetlinks.json 파일 내용
     */
    @GetMapping(value = "/assetlinks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAssetLinks() {
        // TWA 앱과 웹사이트 간의 연결 설정
        // APK 서명 해시값을 실제 값으로 교체해야 함
        String assetLinks = """
            [{
              "relation": ["delegate_permission/common.handle_all_urls"],
              "target": {
                "namespace": "android_app",
                "package_name": "me.jeonjueats.twa",
                "sha256_cert_fingerprints": ["E4:11:7B:CE:AE:5C:A4:06:65:5F:E2:C2:2F:BC:65:70:70:4E:13:35:E8:DD:24:72:47:FD:A8:58:33:82:C9:BC"]
              }
            }]
            """;
        
        return ResponseEntity.ok()
                .header("Content-Type", "application/json; charset=utf-8")
                .body(assetLinks);
    }
}