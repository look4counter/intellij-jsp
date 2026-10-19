# 조사: 2026.1 템플릿 자동완성

## 결정과 근거

- 설치된 IntelliJ IDEA 2026.1.3(build 261.25134.95) API를 기준으로 구현한다. 기본 Gradle 대상은 2026.1.3이며 로컬 SDK 선택 속성을 별도로 제공한다.
- JspLanguage에 TemplateLanguage를 표시하고 ParserDefinition 및 MultiplePsiFilesPerDocumentFileViewProvider를 등록한다.
- HTML_TEXT는 OuterLanguageElementType이다. 일반 토큰이면 기본 JSP root가 HTML의 완성 위치를 차지한다. 다중 view provider는 outer 요소를 건너뛰므로 HTML PSI를 선택할 수 있다.
- HTML root에 TemplateDataElementType을 설정한다. JSP 구간을 HTML 파싱에서 제외한 뒤 outer 구간으로 원본 위치에 복원하므로 완성 삽입 offset을 보존한다.
- HTML parser의 script·style 임베디드 지원을 사용한다. JS·CSS 전체 root를 별도로 추가하지 않는다.
- 기본 parser는 토큰을 모두 소비하는 단순 구조이고 JSP 파일은 PsiFileBase를 사용한다.
- 기존 에디터 강조와 기본 저장 기능은 유지한다.

## 대안

- 구문 강조만 재사용하면 PSI가 없어 기존 자동완성이 작동하지 않는다.
- 수동으로 다른 파일에서 완성을 호출하면 offset·삽입 핸들러·참조 문맥 복제가 필요해 복잡하다.
- 독자 후보 목록은 기존 언어 기능 재사용이라는 요청을 충족하지 못한다.

## 확인한 API

2026.1.3의 javap으로 TemplateLanguage, TemplateLanguageFileViewProvider, TemplateDataElementType, OuterLanguageElementType, MultiplePsiFilesPerDocumentFileViewProvider와 ParserDefinition 시그니처를 확인했다. HTML PSI의 setContentElementType은 트리를 로드하기 전에 호출한다. cloneInner는 복사 문서에 새 view provider를 만들어 완성용 문서 복사를 지원한다.

## 공식 자료

- [다중 언어 파일 view provider](https://plugins.jetbrains.com/docs/intellij/file-view-providers.html)
- [템플릿 언어 구현 안내](https://intellij-support.jetbrains.com/hc/en-us/community/posts/206765105-Tutorial-Custom-templating-language-plugin)
- [기존 완성 체계](https://plugins.jetbrains.com/docs/intellij/code-completion.html)
- [2026.1 무료 웹 기능](https://blog.jetbrains.com/idea/2026/03/js-ts-free-support/)

미해결 기술 결정은 없다.

## 검증 환경 보완

2026.1 배포본에는 testFramework.jar가 없으므로 기본 테스트 의존성은 대상 버전의 TestFrameworkType.Platform을 사용한다. 오프라인 검증에서 로컬 테스트 프레임워크를 명시할 수 있도록 localTestFrameworkPath 속성을 제공한다. 다른 버전의 로컬 프레임워크를 사용한 경우 대상 SDK 버전과 테스트 도구 버전을 검증 기록에서 구분한다.
