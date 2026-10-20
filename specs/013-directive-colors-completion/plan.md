# 구현 계획: 지시자 세부 색상과 자동완성

## 기술 문맥과 요약

Kotlin/JVM, IntelliJ 261 SDK와 기존 계층형 강조·CompletionContributor를 사용한다. 새로운 외부 의존성이나 영구 상태는 없다. 기본 JSP lexer와 PSI 토큰을 유지해 기존 탐색·주입·포매팅을 보존한다.

## 설계와 변경 파일

- src/main/kotlin/com/look4counter/jsp/JspDirectiveSyntaxHighlighter.kt: 바깥 lexer로 지시자 경계를 다시 분리하고 구분자·이름·속성·등호·문자열·공백 토큰을 제공한다. 지시자별 따옴표 상태를 초기화한다.
- JspSyntaxHighlighter.kt: 테마 기본 역할을 따르는 색상 키를 추가한다.
- JspEditorHighlighterProvider.kt: DIRECTIVE 레이어를 연결한다.
- JspDirectiveCompletionContributor.kt: 기존 문맥 파서를 유지하고 import 현재 항목의 클래스·패키지, include 현재 경로 요소를 완성한다. 취소·인덱싱 검사를 적용한다. 구분 문자 자동 팝업과 속성 선택 후 값 팝업을 지원한다.
- JspDirectiveAutoPopupHandler.kt와 plugin.xml: 현재 SDK에서 사용되는 typedHandler로 지시자 구분 문자 입력의 팝업을 예약한다. 구형 contributor 팝업 훅은 사용하지 않는다.
- JspIncludeContext.kt: 기존 include 해석과 같은 기준의 디렉터리 탐색을 공통화한다. 기존 경로 해석 동작은 보존한다.
- src/test/kotlin/com/look4counter/jsp/JspDirectiveAppearanceTest.kt: 의미별 색상 키·미완성·여러 지시자·테마·증분 편집을 검증한다.
- src/test/kotlin/com/look4counter/jsp/JspDirectiveValueCompletionTest.kt: 실제 후보 선택·목록·경로·따옴표·커서·비대상·자동 팝업 계약을 검증한다.
- README.md: 사용자 색상·완성 범위를 갱신한다.

## 편집 정책

현재 import 항목 또는 경로 마지막 요소만 교체한다. 기존 앞부분·쉼표 목록·따옴표·접미사는 보존한다. 클래스 삽입은 완전한 이름이며 프로젝트 해석 범위를 따른다. 패키지·폴더 선택은 다음 구간 완성을 이어갈 수 있다. HTML·Java·EL은 기존 기능을 유지한다.

## 헌장 점검

헌장은 미작성 템플릿이다. 한글 문서와 speckit 순서를 준수하며 설계 전후 위반 없음.
