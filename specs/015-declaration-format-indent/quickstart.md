# 검증 안내: 선언부 포매팅

사용자 예시를 전체 재정렬해 `<%!`와 `%>`가 0열, 함수가 4칸, return이 8칸인지 확인한다. 반복 실행 결과는 같아야 한다. HTML 내부·Java 설정·선택 범위·문자열 및 주석을 별도로 검증한다.

로컬 IntelliJ 2026.1.3 SDK와 호환 테스트 프레임워크를 준비한다. [기존 로컬 검증 명령](../009-java-auto-import/quickstart.md)으로 `test buildPlugin verifyPluginProjectConfiguration`을 실행한다. 배포 ZIP 내부 포매터 등록과 클래스 포함 여부를 확인한다.

## 검증 결과

- 수정 전 사용자 예시에서 선언부의 8칸 들여쓰기와 함수·종료 구분자 정렬 오류를 자동 테스트로 재현했다.
- 신규 선언부 테스트 9개를 포함한 전체 150개 테스트가 실패·오류 없이 통과했다.
- 사용자 예시의 정확한 출력, HTML 중첩, 서로 다른 Java·HTML 들여쓰기, Java 함수 중괄호·인자·연산자 스타일, 탭, 여러 선언부, 빈 선언부, 미완성 코드, 원문 문자열·주석, 전체·부분 선택, 큰 선행 공백 삭제와 반복 실행을 확인했다.
- `test buildPlugin verifyPluginProjectConfiguration`이 성공했다. 로컬 2026.1.3 SDK와 호환 테스트 프레임워크를 사용하고 코드 계측은 비활성화했다.
- `build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip` 내부 JAR에서 선언부 후처리 등록과 관련 포매터 클래스를 확인했다. 테스트 클래스는 포함되지 않았다.
- `git diff --check`가 통과했다. 실제 IDE에서 키보드로 실행하는 수동 확인은 수행하지 않았다.
