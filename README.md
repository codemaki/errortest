# Error Simulation Spring Boot Application

다양한 에러 케이스를 시뮬레이션하는 Spring Boot 애플리케이션입니다.

## 실행 방법

```bash
# Maven으로 빌드 및 실행
mvn clean package
mvn spring-boot:run

# 또는 JAR 파일 실행
java -jar target/error-app-1.0.0.jar
```

애플리케이션은 기본적으로 `http://localhost:8080`에서 실행됩니다.

## API 엔드포인트

### 1. 로그 에러

#### ERROR 레벨 로그
```bash
curl http://localhost:8080/api/error/log/error
```

#### WARN 레벨 로그
```bash
curl http://localhost:8080/api/error/log/warn
```

#### 스택 트레이스 포함 예외 로그
```bash
curl http://localhost:8080/api/error/log/exception
```

#### 대량 에러 로그 생성
```bash
curl "http://localhost:8080/api/error/log/spam?count=100"
```

---

### 2. 일반 예외 (처리되지 않음)

#### NullPointerException
```bash
curl http://localhost:8080/api/error/exception/npe
```

#### ArrayIndexOutOfBoundsException
```bash
curl http://localhost:8080/api/error/exception/array
```

#### NumberFormatException
```bash
curl http://localhost:8080/api/error/exception/number
```

#### Custom Business Exception
```bash
curl http://localhost:8080/api/error/exception/custom
```

#### IOException
```bash
curl http://localhost:8080/api/error/exception/io
```

---

### 3. 치명적 에러 (애플리케이션 종료 위험)

#### StackOverflowError (무한 재귀)
⚠️ **경고**: 애플리케이션이 크래시될 수 있습니다.
```bash
curl http://localhost:8080/api/error/fatal/stackoverflow
```

#### OutOfMemoryError
⚠️ **경고**: 애플리케이션이 크래시될 수 있습니다.
```bash
curl http://localhost:8080/api/error/fatal/oom
```

#### 강제 종료 (System.exit)
⚠️ **경고**: 애플리케이션이 즉시 종료됩니다.
```bash
# Exit code 1로 종료
curl http://localhost:8080/api/error/fatal/exit

# 커스텀 Exit code로 종료
curl "http://localhost:8080/api/error/fatal/exit?code=137"
```

#### 데드락 발생
```bash
curl http://localhost:8080/api/error/fatal/deadlock
```

---

### 4. 성능 관련 이슈

#### 느린 응답 (타임아웃 시뮬레이션)
```bash
# 30초 대기 (기본값)
curl http://localhost:8080/api/error/timeout/slow

# 커스텀 대기 시간
curl "http://localhost:8080/api/error/timeout/slow?seconds=60"
```

#### 메모리 누수 시뮬레이션
```bash
# 1000개 객체 추가 (기본값)
curl http://localhost:8080/api/error/leak/memory

# 커스텀 개수
curl "http://localhost:8080/api/error/leak/memory?count=10000"
```

#### CPU 과부하
```bash
# 10초간 CPU 부하 (기본값)
curl http://localhost:8080/api/error/load/cpu

# 커스텀 시간
curl "http://localhost:8080/api/error/load/cpu?seconds=30"
```

---

### 5. HTTP 상태 코드 에러

#### 4xx 클라이언트 에러
```bash
curl http://localhost:8080/api/error/http/400  # Bad Request
curl http://localhost:8080/api/error/http/401  # Unauthorized
curl http://localhost:8080/api/error/http/403  # Forbidden
curl http://localhost:8080/api/error/http/404  # Not Found
```

#### 5xx 서버 에러
```bash
curl http://localhost:8080/api/error/http/500  # Internal Server Error
curl http://localhost:8080/api/error/http/503  # Service Unavailable
```

---

### 6. 기타

#### 랜덤 에러
매번 다른 종류의 에러가 발생합니다.
```bash
curl http://localhost:8080/api/error/random
```

#### 애플리케이션 상태 확인
```bash
curl http://localhost:8080/api/error/status
```

---

## Actuator 엔드포인트

Spring Boot Actuator를 통해 추가 모니터링이 가능합니다:

```bash
# 헬스 체크
curl http://localhost:8080/actuator/health

# 메트릭
curl http://localhost:8080/actuator/metrics

# 스레드 덤프 (데드락 확인용)
curl http://localhost:8080/actuator/threaddump

# 힙 덤프 (메모리 분석용)
curl http://localhost:8080/actuator/heapdump -o heapdump.hprof
```

---

## 에러 시나리오 테스트 예제

### 시나리오 1: 간단한 에러 로그 테스트
```bash
# 단순 에러 로그만 남기고 정상 응답
curl http://localhost:8080/api/error/log/error
```

### 시나리오 2: 예외 발생 및 스택 트레이스 확인
```bash
# NullPointerException 발생
curl http://localhost:8080/api/error/exception/npe
```

### 시나리오 3: 애플리케이션 강제 종료
```bash
# 1초 후 애플리케이션 종료
curl http://localhost:8080/api/error/fatal/exit
```

### 시나리오 4: 메모리 누수 재현
```bash
# 여러 번 호출하여 메모리 사용량 증가
for i in {1..10}; do
  curl "http://localhost:8080/api/error/leak/memory?count=5000"
  curl http://localhost:8080/api/error/status | jq '.memory'
done
```

### 시나리오 5: 데드락 발생 후 스레드 덤프 확인
```bash
# 데드락 발생
curl http://localhost:8080/api/error/fatal/deadlock

# 스레드 덤프로 데드락 확인
curl http://localhost:8080/actuator/threaddump | less
```

---

## 주의사항

- `fatal/*` 엔드포인트는 애플리케이션을 크래시시킬 수 있습니다.
- `leak/memory`를 반복 호출하면 실제로 메모리 부족이 발생할 수 있습니다.
- `load/cpu`는 시스템 리소스를 많이 사용합니다.
- 프로덕션 환경에서는 절대 사용하지 마세요!

---

## 로그 확인

애플리케이션 로그는 콘솔에 출력됩니다. 로그 레벨:
- `root`: INFO
- `com.example.errorapp`: DEBUG

---

## 시스템 요구사항

- Java 17 이상
- Maven 3.6 이상

---

## 라이선스

테스트 및 학습 목적으로만 사용하세요.
