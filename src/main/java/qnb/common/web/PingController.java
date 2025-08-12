package qnb.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PingController {

    // 루트: 컨텐츠 필요 없으니 204로 응답 (HEAD/GET 모두 커버)
    @RequestMapping(path = "/", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<Void> root() {
        return ResponseEntity.noContent().build(); // 204
    }

    // 헬스/프록시 헤더 점검용
    @GetMapping("/api/ping")
    public ResponseEntity<String> ping(HttpServletRequest req) {
        String proto = req.getHeader("X-Forwarded-Proto");
        return ResponseEntity.ok("ok proto=" + proto);
    }
}
