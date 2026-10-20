# 구현 계획: Ctrl+O import 정리 기능 제거

**명세**: [spec.md](spec.md)

## 기술 문맥과 설계

Kotlin/JVM과 로컬 IntelliJ 261 SDK를 사용한다. 새로운 의존성은 없다. 기능 추가 단계의 변경 중 정리 전용 코드만 제거한다.

- JspImportOptimizer.kt, JspOptimizeImportsAction.kt, JspImportOptimizerTest.kt를 삭제한다.
- plugin.xml에서 정리 액션·Ctrl+O·ActionPromoter 등록을 제거한다.
- JspIncludeContext.kt에서 정리 전용 완전성·방문 파일 필드를 제거하고 기존 include 탐색을 복원한다.
- README.md에서 정리 기능 설명과 링크를 제거한다.
- 기존 Java 자동 import를 포함한 전체 회귀와 배포 ZIP을 검증한다.

## 헌장 점검

헌장은 미작성 템플릿이다. 한글 문서와 speckit 절차를 준수한다. 기존 산출물을 갱신하며 설계 전후 위반 없음.

## 검증

기존 전체 테스트·buildPlugin·verifyPluginProjectConfiguration을 실행한다. ZIP에 정리 클래스·액션·단축키가 없는지 확인한다.