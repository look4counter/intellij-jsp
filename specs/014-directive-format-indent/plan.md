# 구현 계획: 연속 지시자 포매팅 정렬

## 요약과 기술 문맥

Kotlin/JVM과 IntelliJ 261 SDK의 기존 TemplateLanguageFormattingModelBuilder를 사용한다. 별도 후처리로 문서 앞 공백을 지우지 않고 지시자 블록의 들여쓰기 계약을 명시한다. 새 의존성과 영구 상태는 없다.

## 설계

- src/main/kotlin/com/look4counter/jsp/JspFormattingModelBuilder.kt: DIRECTIVE leaf의 추가 들여쓰기는 없앤다. DIRECTIVE_HOST는 직접 HTML 부모 원본 블록의 자식 들여쓰기를 따르며 부모가 없으면 추가 들여쓰기를 없앤다. HTML의 일반 들여쓰기 크기를 사용하고 원본 자식 인덱스를 계산한다. Enter 처리기는 변경하지 않는다.
- src/test/kotlin/com/look4counter/jsp/JspDirectiveFormattingTest.kt: 사용자 예시와 이미 잘못 들여쓴 예시, 반복 재정렬, doctype·여러 줄·주석·빈 줄 보존, HTML 내부 및 선택 영역 재정렬을 검증한다.
- README.md: 최상위 지시자 정렬 동작을 설명한다.

## 검증과 위험

기존 getIndent가 null이므로 기본 continuation indent가 선택될 가능성이 있다. 실제 재정렬 테스트로 수정 전후를 확인한다. HTML 부모 구조를 제거하거나 전체 formatter의 들여쓰기를 고정하면 중첩 HTML이 손상되므로 지시자 블록만 제한한다.

## 헌장 점검

헌장은 미작성 템플릿이다. 한글 문서·speckit 순서를 준수하며 설계 전후 위반 없음.
