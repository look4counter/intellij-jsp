# 구현 계획: JSP 웹 코드 포매팅

**날짜**: 2026-10-19 | **명세**: [spec.md](spec.md)
**작업 식별자**: 004-jsp-formatting. 현재 폴더는 Git 저장소가 아니므로 브랜치를 생성하지 않는다.

## 요약

기존 템플릿 PSI에 플랫폼 템플릿 포매팅 모델을 연결한다. HTML 포매터가 생성한 블록을 재사용하고 JSP 서버 토큰을 불투명한 말단 블록으로 합쳐 내부 원문을 보존한다.

## 기술 환경

- 언어: Kotlin 2.3.20, JDK 21 이상, 로컬 검증 JBR 25.
- 플랫폼: IntelliJ IDEA 2026.1.3, 빌드 261.25134.95.
- 의존성: 플랫폼과 XML 모듈. JS·CSS 제품 클래스에 직접 의존하지 않는다.
- 저장소: 추가 영속 데이터 없음.
- 검증: JUnit 및 BasePlatformTestCase 통합 검증, 기존 회귀 테스트, 빌드와 패키지 설정 검사.
- 범위: 파일과 선택 영역 코드 재정렬. 서버 본문의 세부 포매팅 제외.
- 제약: 서버 토큰 내부 공백까지 보존한다. 기존 HTML·웹 코드 스타일을 사용하고 반복 실행 결과를 유지한다.

## 헌장 점검

헌장이 미작성 템플릿이므로 규범 검사는 생략한다. 사용자 지침에 따라 Markdown은 한글로 작성하고 speckit 전 단계를 진행한다. 설계 후 재점검 결과도 동일하다.

## 0단계: 연구

[research.md](research.md)에 SDK API와 포매터 연결 근거를 기록한다. speckit-plan 지시에 따라 읽기 전용 연구 에이전트로 API를 교차 확인한다.

## 1단계: 설계

- JspFormattingModelBuilder는 TemplateLanguageFormattingModelBuilder를 확장한다.
- JspFormattingBlock은 HTML_TEXT를 데이터 언어로 식별하고 서버 구문을 말단 블록으로 보존한다.
- 플랫폼은 HTML 포매터의 블록을 서버 블록과 병합하고 문서 기반 모델로 공백만 변경한다.
- 문서 기반 모델의 여러 줄 들여쓰기 이동은 서버 말단 노드에서 차단하여 내부 공백까지 보존한다. 나머지 모델 동작은 플랫폼에 위임한다.
- plugin.xml에 CommunityJSP의 lang.formatter를 등록한다.
- HTML·JS·CSS 비교, 설정 반영, 서버 문자열 보존, 선택 영역, 미완성 입력과 반복 실행을 테스트한다.
- 외부 포매터 프로세스나 별도 편집 명령을 도입하지 않는다.

## 프로젝트 구조

- src/main/kotlin/com/look4counter/jsp/JspFormattingModelBuilder.kt: 모델과 템플릿 블록.
- src/main/resources/META-INF/plugin.xml: 포매터 등록.
- src/test/kotlin/com/look4counter/jsp/JspFormattingTest.kt: 실제 코드 스타일 서비스를 통한 통합 검증.
- README.md: 사용법과 범위.
- specs/004-jsp-formatting/: 명세, 연구, 데이터 모델, 계약, 검증 안내, 작업 목록.

## 복잡도

새 제품 의존성이나 설정 화면 없이 플랫폼 템플릿 포매터를 사용한다. 검증 환경은 앞 기능의 오프라인 호환 테스트 도구를 재사용하며 배포물에 포함하지 않는다.
