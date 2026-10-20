# 검증 안내

기존 [로컬 검증 명령](../009-java-auto-import/quickstart.md)의 SDK와 호환 테스트 도구를 사용해 `test buildPlugin verifyPluginProjectConfiguration`을 실행한다.

선언부 호스트에 대해 플랫폼의 실제 배경 적용 여부 API를 검사하고, Java PSI 주입과 원문 보존을 확인한다. 스크립틀릿·표현식의 기존 정책도 함께 검증한다. 함수·필드·빈·미완성·복수 선언부 및 본문 갱신을 확인한다.

수동 확인 시 밝은 테마와 어두운 테마에서 함수를 포함한 `<%! ... %>`를 열어 본문 영역의 별도 배경이 없고 Java 구문 색상이 유지되는지 확인한다.

## 검증 결과

2026-10-21, 기존 로컬 SDK와 호환 테스트 도구로 전체 테스트 및 `buildPlugin verifyPluginProjectConfiguration`이 성공했다. 신규 배경 정책 테스트 3개가 통과했다. 플랫폼 배경 판단 API를 통해 선언부만 배경 억제됨을 확인했고 Java 주입, 원문, 갱신 및 기존 편집·포매팅 회귀를 검증했다.

배포 ZIP `build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip`에 선언부 전용 호스트 클래스가 포함되었다. 실제 IDE 화면에서 테마별 수동 확인은 수행하지 않았다. 기존 테스트의 사용 중단 API 경고는 남아 있다.

구현 후 speckit-converge에서 명세·계획·완료 작업과 현재 호스트 생성·배경 정책·검증 결과를 대조했다. FR-001~003과 SC-001~003에 미구현 차이는 없으며 추가 수렴 작업은 없다.
