# 연구: 템플릿 포매터 재사용

## 결정

TemplateLanguageFormattingModelBuilder와 TemplateLanguageBlock을 사용한다. 261 SDK의 javap 출력과 연구 에이전트의 독립 확인으로 실제 시그니처를 검증했다.

## 근거

부모 모델은 TemplateLanguageFileViewProvider에서 데이터 언어를 얻어 LanguageFormatting의 HTML 모델을 생성한다. 데이터 블록을 DataLanguageBlockWrapper로 감싸고 서버 토큰과 병합한다. HTML 트리의 OuterLanguageElement는 중복 블록에서 제외된다. 기존 데이터 블록의 들여쓰기·정렬·간격은 원본에 위임된다. 서버 토큰은 하나의 말단 블록이므로 내부 문자열을 보존한다.

HTML의 임베디드 모델이 제공하는 JS·CSS 포매팅을 재사용하므로 제품별 포매터에 직접 의존하지 않는다. 속성과 웹 코드 안에 서버 토큰이 있는 사례를 반드시 검증한다.

## 구현 중 확인된 보완

통합 테스트에서 문서 기반 모델이 여러 줄 말단 블록 내부까지 들여쓰기를 이동시키는 것을 확인했다. 서버 토큰을 말단으로 만드는 것만으로는 내부 공백 불변 조건을 충족하지 못한다. 모델의 shiftIndentInsideRange를 서버 말단 노드에 한해 변경 없이 반환하고 나머지 동작은 기존 모델에 위임한다.

## 대안

- HTML 모델을 그대로 반환: 템플릿 구문 보존과 병합 처리를 직접 보장하기 어려워 제외한다.
- 문자열을 치환한 임시 HTML 문서 포매팅: 위치 복구와 사용자 텍스트 보존이 복잡하여 제외한다.
- 독자적인 HTML·JS·CSS 포매터: 기존 코드 스타일과 언어 제공자 재사용 목표에 맞지 않아 제외한다.

## 근거 자료

- [공식 포매터 문서](https://plugins.jetbrains.com/docs/intellij/code-formatting.html)
- 설치된 2026.1.3 SDK의 TemplateLanguageFormattingModelBuilder, TemplateLanguageBlock, DataLanguageBlockWrapper 바이트코드와 공개 API.
- 연구 과정에 미해결 기술 질문은 없다.
