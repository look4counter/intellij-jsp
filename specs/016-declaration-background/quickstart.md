# 검증 안내

[기존 로컬 검증 명령](../009-java-auto-import/quickstart.md)의 SDK·호환 테스트 도구를 사용해 `test buildPlugin verifyPluginProjectConfiguration`을 실행한다.

선언부·스크립틀릿·표현식을 섞은 JSP에서 플랫폼 배경 판단 API가 모두 배경 억제를 반환하고 Java PSI와 원문이 보존되는지 확인한다. 세 종류의 빈·미완성 본문과 갱신도 확인한다.

수동 확인 시 밝은 테마와 어두운 테마에서 세 영역 모두 별도 배경이 없고 Java 구문 색상이 유지되는지 확인한다. 실제 수동 확인 여부와 자동 검증 결과는 완료 후 기록한다.

## 검증 결과

2026-10-21, 수정 전 확장 테스트 3개 중 2개가 스크립틀릿·표현식 배경 정책 때문에 실패하여 문제를 재현했다. 수정 후 전체 테스트 153개가 통과했고 실패·오류는 0건이다. 로컬 SDK와 기존 호환 테스트 도구로 검증했으며 buildPlugin 및 verifyPluginProjectConfiguration도 성공했다. 실제 IDE 화면의 테마별 수동 확인은 수행하지 않았다.

설치 파일: build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip. 설치 후 IDE를 재시작한다.
