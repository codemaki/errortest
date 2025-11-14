package com.example.errorapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/error")
public class ErrorController {

    private static final Logger log = LoggerFactory.getLogger(ErrorController.class);
    private final List<String> memoryLeakList = new ArrayList<>();

    /**
     * 1. 단순 로그 에러 - ERROR 레벨 로그만 출력
     */
    @GetMapping("/log/error")
    public ResponseEntity<Map<String, String>> logError() {
        log.error("에러 로그가 발생했습니다! timestamp: {}", System.currentTimeMillis());
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "ERROR 로그가 기록되었습니다."
        ));
    }

    /**
     * 2. 경고 로그
     */
    @GetMapping("/log/warn")
    public ResponseEntity<Map<String, String>> logWarn() {
        log.warn("경고 로그입니다! 잠재적 문제가 발견되었습니다.");
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "WARN 로그가 기록되었습니다."
        ));
    }

    /**
     * 3. 스택 트레이스를 포함한 예외 로그
     */
    @GetMapping("/log/exception")
    public ResponseEntity<Map<String, String>> logException() {
        try {
            throw new RuntimeException("의도적으로 발생시킨 예외입니다.");
        } catch (Exception e) {
            log.error("예외가 발생했습니다!", e);
        }
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "예외 로그(스택 트레이스 포함)가 기록되었습니다."
        ));
    }

    /**
     * 4. NullPointerException 발생
     */
    @GetMapping("/exception/npe")
    public ResponseEntity<String> nullPointerException() {
        String str = null;
        return ResponseEntity.ok(str.length() + ""); // NPE 발생
    }

    /**
     * 5. ArrayIndexOutOfBoundsException 발생
     */
    @GetMapping("/exception/array")
    public ResponseEntity<String> arrayIndexException() {
        int[] array = new int[5];
        return ResponseEntity.ok(String.valueOf(array[10])); // 배열 범위 초과
    }

    /**
     * 6. NumberFormatException 발생
     */
    @GetMapping("/exception/number")
    public ResponseEntity<Integer> numberFormatException() {
        return ResponseEntity.ok(Integer.parseInt("not-a-number"));
    }

    /**
     * 7. Custom Exception 발생
     */
    @GetMapping("/exception/custom")
    public ResponseEntity<String> customException() {
        throw new BusinessException("비즈니스 로직 오류가 발생했습니다.");
    }

    /**
     * 8. StackOverflowError - 무한 재귀
     */
    @GetMapping("/fatal/stackoverflow")
    public ResponseEntity<String> stackOverflowError() {
        return ResponseEntity.ok(recursiveMethod());
    }

    private String recursiveMethod() {
        return recursiveMethod(); // 무한 재귀
    }

    /**
     * 9. OutOfMemoryError - 메모리 과다 사용
     */
    @GetMapping("/fatal/oom")
    public ResponseEntity<String> outOfMemoryError() {
        List<byte[]> list = new ArrayList<>();
        while (true) {
            list.add(new byte[1024 * 1024 * 10]); // 10MB씩 할당
        }
    }

    /**
     * 10. System.exit() - 강제 종료
     */
    @GetMapping("/fatal/exit")
    public ResponseEntity<String> systemExit(@RequestParam(defaultValue = "1") int code) {
        log.error("애플리케이션을 강제로 종료합니다. Exit code: {}", code);
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 응답 반환 후 종료
                System.exit(code);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        return ResponseEntity.ok("1초 후 애플리케이션이 종료됩니다.");
    }

    /**
     * 11. 데드락 발생
     */
    @GetMapping("/fatal/deadlock")
    public ResponseEntity<String> createDeadlock() {
        Object lock1 = new Object();
        Object lock2 = new Object();

        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                log.info("Thread 1: lock1 획득");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                synchronized (lock2) {
                    log.info("Thread 1: lock2 획득");
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (lock2) {
                log.info("Thread 2: lock2 획득");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                synchronized (lock1) {
                    log.info("Thread 2: lock1 획득");
                }
            }
        });

        thread1.start();
        thread2.start();

        return ResponseEntity.ok("데드락이 발생했습니다. 스레드 덤프를 확인하세요.");
    }

    /**
     * 12. 느린 응답 - 타임아웃 시뮬레이션
     */
    @GetMapping("/timeout/slow")
    public ResponseEntity<String> slowResponse(@RequestParam(defaultValue = "30") int seconds) {
        try {
            log.warn("{}초 대기 시작...", seconds);
            TimeUnit.SECONDS.sleep(seconds);
            return ResponseEntity.ok(seconds + "초 대기 완료");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("대기 중 인터럽트 발생", e);
        }
    }

    /**
     * 13. HTTP 4xx 에러
     */
    @GetMapping("/http/400")
    public ResponseEntity<Map<String, String>> badRequest() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", "잘못된 요청입니다."));
    }

    @GetMapping("/http/401")
    public ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "인증이 필요합니다."));
    }

    @GetMapping("/http/403")
    public ResponseEntity<Map<String, String>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "권한이 없습니다."));
    }

    @GetMapping("/http/404")
    public ResponseEntity<Map<String, String>> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "리소스를 찾을 수 없습니다."));
    }

    /**
     * 14. HTTP 5xx 에러
     */
    @GetMapping("/http/500")
    public ResponseEntity<Map<String, String>> internalServerError() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "서버 내부 오류가 발생했습니다."));
    }

    @GetMapping("/http/503")
    public ResponseEntity<Map<String, String>> serviceUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", "서비스를 사용할 수 없습니다."));
    }

    /**
     * 15. 메모리 누수 시뮬레이션
     */
    @GetMapping("/leak/memory")
    public ResponseEntity<Map<String, String>> memoryLeak(@RequestParam(defaultValue = "1000") int count) {
        for (int i = 0; i < count; i++) {
            memoryLeakList.add("메모리 누수 데이터: " + UUID.randomUUID().toString());
        }
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", count + "개의 객체가 메모리에 추가되었습니다.",
            "totalSize", String.valueOf(memoryLeakList.size())
        ));
    }

    /**
     * 16. CPU 과부하
     */
    @GetMapping("/load/cpu")
    public ResponseEntity<Map<String, String>> cpuLoad(@RequestParam(defaultValue = "10") int seconds) {
        long endTime = System.currentTimeMillis() + (seconds * 1000L);
        int count = 0;

        while (System.currentTimeMillis() < endTime) {
            Math.sqrt(Math.random()); // CPU 집약적 작업
            count++;
        }

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", seconds + "초 동안 CPU 부하 생성 완료",
            "iterations", String.valueOf(count)
        ));
    }

    /**
     * 17. IOException 시뮬레이션
     */
    @GetMapping("/exception/io")
    public ResponseEntity<String> ioException() throws IOException {
        throw new IOException("파일을 읽을 수 없습니다.");
    }

    /**
     * 18. 연속적인 에러 로그
     */
    @GetMapping("/log/spam")
    public ResponseEntity<Map<String, String>> spamLogs(@RequestParam(defaultValue = "100") int count) {
        for (int i = 0; i < count; i++) {
            log.error("에러 로그 #{}: 심각한 문제가 발생했습니다!", i + 1);
        }
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", count + "개의 에러 로그가 생성되었습니다."
        ));
    }

    /**
     * 19. 랜덤 에러
     */
    @GetMapping("/random")
    public ResponseEntity<String> randomError() {
        Random random = new Random();
        int errorType = random.nextInt(5);

        switch (errorType) {
            case 0:
                throw new NullPointerException("랜덤 NPE 발생");
            case 1:
                throw new IllegalArgumentException("랜덤 IllegalArgumentException 발생");
            case 2:
                throw new IllegalStateException("랜덤 IllegalStateException 발생");
            case 3:
                log.error("랜덤 에러 로그 발생");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("랜덤 서버 에러");
            default:
                return ResponseEntity.ok("성공 (랜덤)");
        }
    }

    /**
     * 20. 애플리케이션 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Runtime runtime = Runtime.getRuntime();
        return ResponseEntity.ok(Map.of(
            "status", "running",
            "memory", Map.of(
                "total", runtime.totalMemory(),
                "free", runtime.freeMemory(),
                "used", runtime.totalMemory() - runtime.freeMemory(),
                "max", runtime.maxMemory()
            ),
            "processors", runtime.availableProcessors(),
            "leakListSize", memoryLeakList.size()
        ));
    }
}

/**
 * 커스텀 예외 클래스
 */
class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
