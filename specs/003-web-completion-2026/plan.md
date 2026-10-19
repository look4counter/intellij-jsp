# 구현 계획: 2026.1 JSP 웹 자동완성

**작성일**: 2026-10-19 | **명세**: [spec.md](spec.md)
**브랜치**: 없음

## 요약

IntelliJ IDEA 2026.1.3을 기본 대상으로 바꾸고 JSP 템플릿의 HTML PSI를 제공한다. HTML parser가 기존 script·style 구문 트리를 구성하게 하여 기존 완성 기능을 재사용한다. 별도 후보 목록과 수동 offset 변환은 만들지 않는다.

## 기술 환경

- Kotlin 2.3.20, Gradle 9.7.1, IntelliJ Platform Gradle Plugin 2.19.0.
- 대상 IDE 2026.1.3, 최소 빌드 261. 로컬 검증 SDK는 설치된 2026.1.3을 localIdePath 속성으로 지정한다.
- 컴파일·테스트 JDK는 대상 SDK의 기본 요구사항에 맞춘다. 설치된 IDE에는 JBR 25가 포함되어 있다.
- 필수 플랫폼·XML 의존성을 유지하고 JS·CSS는 기존 활성 기능에 위임한다.
- 기본 테스트 의존성은 대상 버전의 Platform 프레임워크를 사용한다. 오프라인 개발 시 localTestFrameworkPath로 별도 로컬 테스트 도구를 지정할 수 있다.
- 기존 강조 테스트와 플랫폼 기반 PSI·자동완성 후보·선택 삽입 테스트를 실행한다.
- 별도 저장소·외부 서비스는 없다. 기존 문서를 사용한다.

## 원칙 점검

헌법은 미작성 템플릿이므로 구체 원칙 검사를 생략한다. 한글 문서 및 speckit 전체 순서를 따른다. 설계 전후 위반 없음.

## 조사 및 설계

[조사](research.md)를 따른다. JspLanguage가 TemplateLanguage임을 선언하고 기본 JSP parser는 기존 lexer의 토큰을 소비해 단순 JSP PSI를 만든다. 기본 JSP 트리의 HTML_TEXT는 OuterLanguageElementType으로 만들어 해당 위치가 HTML PSI에 맡겨지도록 한다.

JspFileViewProvider는 기본 JSP와 HTML root를 제공한다. HTML root의 contentElementType을 TemplateDataElementType으로 바꿔 JSP 영역을 파싱에서 제외하되 결과 구문 트리에 원문 길이와 offset을 보존한다. JSP 내부는 JSP root로, 웹 영역은 HTML 및 기존 임베디드 언어 root로 처리한다. CompletionContributor를 직접 호출하거나 후보를 하드코딩하지 않는다.

기존 LayeredLexerEditorHighlighter와 키를 유지한다. JS·CSS 클래스에 직접 의존하지 않으며 testWebLanguages 구성에서만 웹 플러그인을 명시한다.

## 프로젝트 구조

- `build.gradle.kts`: 2026.1.3 기본 SDK와 선택적 localIdePath, 호환 범위.
- `src/main/kotlin/com/look4counter/jsp/JspLanguage.kt`: 템플릿 언어 표시.
- `src/main/kotlin/com/look4counter/jsp/JspHighlightingLexer.kt`: HTML outer 토큰 및 JSP 언어 토큰 연결.
- `src/main/kotlin/com/look4counter/jsp/JspParserDefinition.kt`: 단순 JSP parser와 PSI 파일.
- `src/main/kotlin/com/look4counter/jsp/JspFileViewProvider.kt`: 다중 PSI와 HTML 템플릿 데이터 유형, factory.
- `src/main/resources/META-INF/plugin.xml`: parser 및 view provider 등록, 기능 설명.
- `src/test/kotlin/com/look4counter/jsp/JspCompletionTest.kt`: PSI 원문·위치, HTML·웹 완성 및 삽입 검증.
- `src/test/testData/completion.jsp`: 수동 완성 확인 예제.
- `README.md`: 2026.1 대상 및 기능·빌드 안내.
- `specs/003-web-completion-2026/`: 설계 및 검증 기록.

## 단계

1. 2026.1 대상과 로컬 SDK 검증 구성을 마련한다.
2. PSI와 HTML 완성 테스트를 먼저 작성한다.
3. 기본 parser와 다중 PSI view provider를 연결한다.
4. 실제 JS·CSS 완성과 원본 위치의 삽입을 검증한다.
5. 기존 강조 회귀와 빌드·구성 검사를 수행하고 문서화한다.
6. converge로 남은 작업을 점검한다.

## 복잡도

HTML 및 JS·CSS parser와 완성 기능을 재사용한다. JSP Java·EL 구문 의미 분석은 추가하지 않는다. 기본 JSP parser는 서버 영역을 구분하는 데 필요한 최소 구조만 제공한다.

