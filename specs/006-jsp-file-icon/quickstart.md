# 검증: IntelliJ 기본 JSP 아이콘

## 자동 검증

buildPlugin verifyPluginProjectConfiguration을 실행한다. 설치 SDK와 로컬 Gradle 인자는 [이전 재현 명령](../005-java-editor/quickstart.md)을 따른다. 컴파일된 getIcon의 반환 필드와 ZIP 안 자체 아이콘 제거 여부를 확인한다.

## 수동 검증

ZIP 설치 및 IDEA 재시작 후 프로젝트 트리와 탭에 IntelliJ 기본 JSP 아이콘이 표시되는지 확인한다. 테마와 배율 처리는 플랫폼에 위임한다.

## 결과

2026-10-20, IntelliJ IDEA 2026.1.3 SDK에서 buildPlugin과 verifyPluginProjectConfiguration이 성공했다. 컴파일된 getIcon이 AllIcons.FileTypes.Jsp를 직접 반환함을 확인했다. 소스·리소스 출력·배포 JAR에 자체 제작 jsp.svg와 jsp_dark.svg가 남지 않았음을 확인했다.

낮은 영향도의 아이콘 연결 변경으로 새 테스트 및 전체 편집 테스트 재실행은 하지 않았다. 실제 IDE 화면에서 수동 설치 확인은 수행하지 않았다. 검증은 기존 로컬 캐시와 SDK를 사용하고 계측을 끈 오프라인 구성이다.

설치 파일: build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip.
