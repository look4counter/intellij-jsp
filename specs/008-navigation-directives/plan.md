# 구현 계획: JSP 이동과 지시자 완성

**작성일**: 2026-10-20 | **명세**: [spec.md](spec.md)

## 요약

지시자를 복합 PSI로 만들고 include file 값에 직접 참조를 제공한다. Java 주입 생성 중 원본 범위 매핑을 저장하고 GotoDeclarationHandler가 해석된 함수의 이름 위치를 원본 JSP로 변환한다. 지시자 전용 CompletionContributor로 문맥별 후보와 삽입을 제공한다.

## 기술 문맥

기존 Kotlin/JVM과 IntelliJ IDEA 2026.1.3, Java 플러그인을 사용한다. 추가 외부 의존성은 없다. 주입 파일 사용자 데이터에 원본 파일·본문 범위·가상 범위를 보관한다. include 경로 정책과 부모 선택은 기존 구현을 재사용한다.

## 헌장 점검

헌장은 미작성 템플릿이다. 한글 문서와 speckit 순서를 준수한다. 설계 전후 위반 없음.

## 설계 산출물

[연구](research.md), [모델](data-model.md), [계약](contracts/editor.md), [검증](quickstart.md)을 따른다.

## 변경 파일

- src/main/kotlin/com/look4counter/jsp/JspDirective.kt: 공통 구문 분석과 PSI·경로 참조.
- JspParserDefinition.kt: 지시자 복합 노드 생성.
- JspIncludeContext.kt: 공통 지시자 구문 및 공개된 내부 경로 해석 재사용.
- JspJavaInjector.kt: 가상 Java의 모든 원본 코드 범위 추적.
- JspJavaSourceMap.kt 및 JspGotoDeclarationHandler.kt: 저장된 범위와 해석된 함수의 원본 이동 대상.
- JspDirectiveCompletionContributor.kt: 지시자 이름·속성·고정 값 완성과 삽입.
- src/main/resources/META-INF/plugin.xml: 선언 이동 핸들러와 완성 기여자 등록.
- src/test/kotlin/com/look4counter/jsp/JspNavigationTest.kt 및 JspDirectiveCompletionTest.kt: 실제 플랫폼 이동과 완성 검증.
- README.md: 지원 동작 안내.

## 단계

1. 테스트를 먼저 작성한다.
2. 지시자 PSI와 include 경로 참조를 구현한다.
3. 소스 범위 매핑과 함수 선언 이동을 연결한다.
4. 지시자 완성과 삽입을 구현한다.
5. 회귀 검증·빌드·패키징 및 수렴 점검한다.

## 제약

함수 이동에는 Java resolve 결과를 사용하고 문자열 이름 검색을 사용하지 않는다. 실제 JSP의 렉서 토큰이 선언 전체를 포함하므로 정확한 식별자 오프셋으로 이동하는 탐색 대상을 반환한다. 주입 매핑은 주입 캐시와 함께 갱신한다. 지시자 참조와 완성은 원문 위치를 유지한다.
