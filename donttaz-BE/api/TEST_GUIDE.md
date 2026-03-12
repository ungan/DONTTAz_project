# 🧪 백엔드 단위 테스트(Unit Test) 작성 가이드라인

우리 팀의 지속적 테스트(CT) 전략은 **"빠르고 격리된 단위 테스트"** 를 핵심으로 합니다.
스프링 부트 컨테이너 전체를 띄우지 않아 속도가 빠르며, 의존성을 분리하여 해당 로직 자체가 올바른지만을 정확하게 검증합니다.

---

## 1. 단위 테스트의 핵심 원칙 (F.I.R.S.T)
- **Fast (빠르게):** 스프링 서버(`@SpringBootTest`) 띄우기 금지. 수 초 내에 전체 테스트가 끝나야 합니다.
- **Independent (독립적으로):** 다른 테스트 결과나 DB 상태에 영향을 받거나 주면 안 됩니다.
- **Repeatable (반복 가능하게):** 로컬, CI(Jenkins) 어디서 실행하든 항상 똑같은 결과(성공/실패)가 나와야 합니다.

---

## 2. 필수 라이브러리 및 어노테이션
단위 테스트는 **JUnit 5**와 **Mockito**만을 활용합니다.

- `@ExtendWith(MockitoExtension.class)`: 스프링 컨테이너 없이 Mock 객체 환경을 세팅합니다. 클래스 상단에 붙입니다.
- `@Mock`: 진짜 객체 대신 사용할 가짜(Mock) 객체를 생성합니다. (주로 Repository나 외부 연동 Client 클래스)
- `@InjectMocks`: 진짜 로직을 테스트할 우리의 '주인공' 객체입니다. 이 어노테이션을 붙이면, 위에서 선언한 `@Mock` 가짜 객체들이 주인공의 생성자로 자동 주입됩니다.

---

## 3. 테스트 코드 구조 (Given - When - Then)
모든 테스트 메서드는 **BBD(Behavior-Driven Development)** 패턴인 `Given-When-Then` 구조를 따릅니다. 주석으로 구역을 나누어 쓰면 읽기 편합니다.

### 💡 실전 예시 (Service 계층 테스트)

```java
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class) // 필수! 스프링을 띄우지 않고 가볍게 Mock 환경 구성
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository; // 가짜 객체

    @InjectMocks
    private AccountService accountService; // 진짜 테스트 대상 (위의 가짜 Repo가 알아서 주입됨)

    @Test
    @DisplayName("정상적인 계좌 ID가 주어지면, 계좌 정보를 반환한다.")
    void getAccount_Success() {
        // [Given] 테스트 환경 준비
        Long targetAccountId = 1L;
        Account mockAccount = Account.builder().id(targetAccountId).balance(10000L).build();
        
        // 행동 지시: "가짜 Repo야, 누군가 findById(1L)을 부르면 이 mockAccount를 던져주렴!"
        given(accountRepository.findById(targetAccountId)).willReturn(Optional.of(mockAccount));

        // [When] 실제 대상 객체의 메서드 실행
        Account result = accountService.getAccount(targetAccountId);

        // [Then] 결과 검증
        assertThat(result.getId()).isEqualTo(targetAccountId);
        assertThat(result.getBalance()).isEqualTo(10000L);
        // (선택) 실제로 Repo의 메서드가 1번 호출되었는지 검증
        verify(accountRepository).findById(targetAccountId);
    }

    @Test
    @DisplayName("존재하지 않는 계좌 ID를 조회하면 CustomException을 던져야 한다.")
    void getAccount_NotFound_ThrowsException() {
        // [Given] 
        Long invalidId = 999L;
        // 행동 지시: "DB에 없으므로 비어있는(Empty) 상태를 반환하라!"
        given(accountRepository.findById(invalidId)).willReturn(Optional.empty());

        // [When & Then] 예외가 터지는지 검증
        assertThrows(CustomException.class, () -> {
            accountService.getAccount(invalidId);
        });
    }
}
```

---

## 4. 백엔드 팀원 행동 수칙
1. **Controller 보다 Service 테스트 우선:** 단위 테스트의 핵심은 화면(Controller)이 아니라 비즈니스 로직(Service)입니다. Service 클래스 위주로 테스트를 짭니다.
2. **Push 전 로컬 검증:** 터미널에서 `./gradlew test` 를 쳐서 내 코드가 기존 로직을 망가뜨리진 않았는지 **반드시 로컬에서 `BUILD SUCCESSFUL`을 보고 Push** 합니다.
3. Jenkins 파이프라인에서 테스트가 1개라도 실패하면 해당 코드는 상용에 배포되지 않고(배포 차단) 팀 채널에 경고가 날아갑니다.
