# 검증 안내: 지시자 Enter

사용자의 page import·page contentType/pageEncoding·include 예시 각 줄의 `%>` 뒤에서 Enter를 누른다. 추가 8칸 없이 다음 줄을 시작하고 기존 선행 공백·탭은 유지해야 한다. 반복 Enter·파일 끝·다음 지시자 앞·커서·실행 취소와 HTML·Java·미완성 지시자를 검증한다.

기존 [로컬 SDK 명령](../009-java-auto-import/quickstart.md)으로 test buildPlugin verifyPluginProjectConfiguration을 실행하고 결과를 기록한다.

## 수정 전 관찰과 제한

기본 설정의 로컬 IntelliJ 261 SDK에서는 사용자 예시의 공백 8칸 현상이 재현되지 않았다. 실제 Enter 액션으로 비물리·물리 JSP를 검증했으며 문서 내용과 커서 논리 열 모두 추가 들여쓰기가 없었다. 지시자는 Java 주입 대상이 아니므로 가상 Java 클래스·메서드의 4+4칸이 원인이라는 초기 추정은 이 사례에 해당하지 않는다.

따라서 사용자 환경의 정확한 원인은 미확정이다. 변경은 독립된 닫힌 지시자 뒤 Enter의 기존 들여쓰기 복제와 후속 계산 중단을 명시적으로 보장한다. 사용자 IDE 화면에서 동일 증상이 해소됐는지는 확인하지 않았다.

## 검증 결과

2026-10-20, 기존 로컬 호환 테스트 도구와 읽기 전용 의존성 캐시 및 실제 261 SDK로 전체 테스트 117개가 통과했다. 신규 Enter 테스트는 10개이며 실패·오류는 0개다. 검증에서만 계측을 껐으며 정식 261 테스트 프레임워크 재검증은 수행하지 않았다.

- 사용자 예시의 각 지시자·지시자 사이·물리 JSP 파일에서 실제 Enter와 커서 열을 확인했다.
- JSPF·선행 공백·탭·taglib·종료 뒤 공백·여러 줄 지시자·반복 Enter·실행 취소를 확인했다.
- 비대상 HTML·Java·인라인·미완성 지시자에서 직접 처리하지 않으며 HTML Enter 결과가 기존 HTML과 같음을 확인했다.
- 실제 등록된 delegate가 Stop을 반환하여 지시자 뒤 기본 후속 들여쓰기 계산을 중단함을 확인했다.
- 기존 import 자동 추가·클래스 이동·include·완성·강조·포매팅 회귀가 통과했다.
- buildPlugin과 verifyPluginProjectConfiguration이 성공했다. ZIP에 Enter 처리기와 우선 등록이 포함되고 테스트 클래스는 포함되지 않는다.

## 수렴 점검

speckit-converge로 FR-001~004, 두 사용자 이야기·편집 계약·T001~T005를 대조했다. 명시한 Enter 정책의 남은 구현 차이는 없다. 사용자 환경의 원인 미확정은 위 제한으로 기록했다. 헌장은 미작성 템플릿으로 원칙 검사를 생략했다.

설치 파일: `build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip`. 설치한 뒤 IDE를 재시작한다.
