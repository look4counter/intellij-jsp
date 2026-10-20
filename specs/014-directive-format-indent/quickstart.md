# 검증 안내: 지시자 포매팅

사용자 예시의 page import·page contentType/pageEncoding·include 뒤에 doctype을 넣고 재정렬한다. 세 지시자와 doctype이 0열이어야 한다. 이미 8칸 들여쓴 예시·반복 실행·다른 따옴표·여러 줄·HTML 내부·선택 영역과 기존 전체 포매팅을 검증한다.

기존 [로컬 SDK 검증 명령](../009-java-auto-import/quickstart.md)으로 test buildPlugin verifyPluginProjectConfiguration을 실행하고 결과를 기록한다.

## 검증 결과

- 수정 전 사용자 예시와 기존 8칸 들여쓰기 예시에서 같은 오류를 자동 테스트로 재현했다.
- 신규 지시자 포매팅 7개와 기존 포매팅·Java 편집을 포함한 관련 테스트 31개가 통과했다.
- 전체 테스트 141개가 실패·오류 없이 통과했다. 최상위 정렬, 반복 실행, 여러 줄·주석 원문 보존, HTML 중첩과 사용자 지정 들여쓰기, 선택 영역 및 JSPF를 검증했다.
- 로컬 IntelliJ 2026.1.3 SDK와 호환 테스트 프레임워크를 사용했고 코드 계측은 비활성화했다. `test buildPlugin verifyPluginProjectConfiguration`이 성공했다.
- `build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip` 내부 JAR의 수정된 포매팅 블록을 확인했으며 테스트 클래스는 포함되지 않았다.
- 실제 IDE에서 키보드로 재정렬하는 수동 확인은 수행하지 않았다.
