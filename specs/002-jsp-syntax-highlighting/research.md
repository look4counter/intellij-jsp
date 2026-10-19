# 조사: 기존 강조기 재사용

## 결정

- LayeredLexerEditorHighlighter와 LayerDescriptor(HtmlFileHighlighter(), 빈 문자열)를 사용한다.
- EditorHighlighterProvider를 언어 파일 유형 Community JSP에 등록한다. XML 속성은 소문자 filetype이다.
- 기본 플랫폼과 com.intellij.modules.xml만 필수로 선언한다. JS·CSS 클래스는 직접 참조하지 않는다.
- 테스트 시 -PtestWebLanguages=true로 testBundledPlugins(JavaScript, com.intellij.css)를 추가해 웹 언어가 있는 구성을 검증한다. 기본 테스트는 해당 의존성 없이 수행한다.

## 근거

로컬 2025.3.6.1의 javap으로 확인한 HtmlFileHighlighter는 HtmlLexer(true)를 반환하고 임베디드 강조 키를 연결한다. HtmlLexer는 HtmlEmbeddedContentSupport로 JS·CSS lexer를 제공받는다. HTML 레이어는 여러 HTML_TEXT 토큰을 내부 문서에 연결하고 그 문서의 변경 리스너로 증분 강조를 갱신한다. 따라서 JSP가 HTML 속성이나 JS 문자열 사이에 있어도 주변 문맥이 이어진다.

## 대안

- JSP 전체를 HTML로 등록하면 JSP 주석·구분자를 오인한다.
- 각 HTML 조각에 독립 lexer를 시작하면 속성·문자열 상태를 잃는다.
- 전체 PSI 템플릿 언어 구현은 자동 완성·오류 검사 단계에서 검토한다.
- 웹 플러그인을 필수 의존성으로 넣으면 무료 환경의 플러그인 가용성에 영향을 받으므로 사용하지 않는다.

## 공식 자료

- [구문 강조](https://plugins.jetbrains.com/docs/intellij/syntax-highlighting-and-error-highlighting.html)
- [렉서와 재시작 상태](https://plugins.jetbrains.com/docs/intellij/implementing-lexer.html)
- [플러그인 및 테스트 전용 의존성](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html)

미해결 기술 결정은 없다.

## 구현 중 확인한 API 제약

2025.3 EditorHighlighterFactoryImpl은 일반 FileType의 경우 editorHighlighterProvider를 조회하지 않고 기본 SyntaxHighlighterFactory를 사용한다. 통합 테스트에서 empty token이 반환되는 것을 확인했고 javap으로 분기를 확인했다. 따라서 JspLanguage를 추가하고 기존 이름과 확장자를 유지한 LanguageFileType으로 전환한다. 파서나 PSI 지원은 추가하지 않는다.

테스트 프레임워크는 대상 IDE에 포함된 TestFrameworkType.Bundled를 사용한다. 별도 원격 프레임워크의 오프라인 의존성 누락을 피하고 동일 빌드의 실제 API를 검증한다. 공식 문서에 안내된 opentest4j 런타임 의존성도 명시한다.

