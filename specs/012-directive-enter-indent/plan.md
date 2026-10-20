# 구현 계획: 지시자 Enter 들여쓰기

## 요약과 기술 문맥

Kotlin/JVM과 IntelliJ 261 SDK의 EnterHandlerDelegate를 사용한다. 기존 formatter는 유지한다. 독립된 닫힌 지시자 뒤 Enter만 처리하며 기본 HTML·Java 입력은 그대로 둔다. 새 의존성이나 영구 상태는 없다.

## 설계

- src/main/kotlin/com/look4counter/jsp/JspDirectiveEnterHandler.kt: JSP 원본·닫힌 지시자·줄 시작과 커서 사이의 공백 범위를 확인하고 시작 줄의 선행 공백·탭을 복제한다. 직접 줄바꿈을 넣고 Stop으로 기본 들여쓰기 중복 실행을 막는다.
- src/main/resources/META-INF/plugin.xml: enterHandlerDelegate를 등록한다.
- src/test/kotlin/com/look4counter/jsp/JspDirectiveEnterTest.kt: 실제 Enter 액션으로 사용자 예시·공백·탭·반복·커서·실행 취소와 비대상 검증을 수행한다.
- README.md: 지시자 뒤 Enter 동작을 설명한다.

## 제약

선택 영역·다중 커서·인라인 또는 미완성 지시자에서는 기본 처리를 사용한다. 쓰기는 기존 Enter 명령 안에서 수행한다. Java 가상 클래스 들여쓰기라는 초기 추정은 지시자 재현 사례에 해당하지 않는다.

## 헌장 점검

헌장은 미작성 템플릿이다. 한글 문서·speckit 순서를 준수하며 설계 전후 위반 없음.