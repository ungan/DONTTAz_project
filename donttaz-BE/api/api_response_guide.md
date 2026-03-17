# 자동 응답 래퍼(GlobalResponseHandler) 사용 가이드

본 문서에서는 새롭게 도입된 **API 응답 자동 래퍼(Global Response Wrapper)** 의 사용법과 동작 원리를 설명합니다.

---

## 🚀 도입 배경
기존에는 프론트엔드와 규약을 맞추기 위해 모든 Controller 반환갑에 `ApiResponse.success(..., data)`를 수동으로 입혀야 했습니다.
이제 [GlobalResponseHandler](file:///c:/Users/SSAFY/Documents/git/S14P21C104/donttaz-BE/api/src/main/java/com/donttaz/donttazapi/common/response/GlobalResponseHandler.java#11-41) (Spring의 `ResponseBodyAdvice`)가 이 역할을 대신 수행합니다. 

따라서 개발자는 Controller에서 **오직 데이터(DTO)만 신경 써서 반환**하면 됩니다. 

---

## 🛠️ 사용 방법 (Controller 작성법)

### ✅ TO-BE: 올바른 컨트롤러 작성 예시 (데이터만 반환)
```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED) // 201 Created 등 상태코드 지정이 필요할 때 사용
    public CreateUserResponse signUp(@Valid @RequestBody CreateUserRequest request) {
        // ApiResponse 로 묶을 필요가 전혀 없습니다!
        return userCommandService.createUser(request);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserSummaryResponse> getMyInfo() {
        // ResponseEntity로 감싸서 넘겨도, ResponseBodyAdvice가 본문(body)만 쏙 빼서 예쁘게 포장해줍니다.
        return ResponseEntity.ok(userQueryService.getUserSummary(userId));
    }
}
```

### ❌ AS-IS: 더 이상 이렇게 하지 마세요!
```java
    @PostMapping("/signup")
    public ApiResponse<CreateUserResponse> signUp(@Valid @RequestBody CreateUserRequest request) {
        CreateUserResponse response = userCommandService.createUser(request);
        // 수동 포장 금지! -> 타입 불일치 에러는 안 나지만, 설계 원칙에 어긋납니다.
        return ApiResponse.success("회원 가입에 성공했습니다.", response);
    }
```

---

## 💡 알아두면 좋은 점

1. **에러 처리는 어떻게 하나요?**
   - 변경 사항 없습니다! 기존처럼 Service나 Controller에서 익셉션을 `throw` 하면, 만들어둔 `@RestControllerAdvice(GlobalExceptionHandler)` 가 낚아채서 알아서 `{ "status": "FAIL", "message": "에러내용" }` 형태로 내려줍니다.
2. **반환값이 없는 (Void) 경우는요?**
   - Controller 메서드의 반환 타입을 `void` 나 `Void` 로 지정하시고 아무것도 리턴하지 않으면(`return null`) 알아서 `{ "status": "SUCCESS", "message": "요청이 성공적으로 처리되었습니다.", "data": null }` 이 반환됩니다.
3. **Swagger 응답은 깨지지 않나요?**
   - Swagger 기본 주소(`/v3/api-docs`, `/swagger-ui`)로 시작하는 요청은 [GlobalResponseHandler](file:///c:/Users/SSAFY/Documents/git/S14P21C104/donttaz-BE/api/src/main/java/com/donttaz/donttazapi/common/response/GlobalResponseHandler.java#11-41)가 자동으로 필터링하여 래핑(Wrapping)하지 않도록 안전장치가 되어있습니다.
