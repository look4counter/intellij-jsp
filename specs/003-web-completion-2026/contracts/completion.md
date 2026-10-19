# 자동완성 계약

- 대상 버전은 IntelliJ IDEA 2026.1 계열이며 파일 유형 이름 Community JSP와 jsp 확장자를 유지한다.
- 기본 JSP·HTML PSI의 텍스트와 길이는 원문과 같아야 한다.
- HTML 위치는 기존 HTML 완성, script·style 위치는 활성화된 기존 웹 언어 완성을 사용한다.
- JSP 블록 위치는 JSP root로 남고 웹 언어 완성을 강제로 호출하지 않는다.
- 후보 선택 시 기존 언어의 삽입 동작을 따르되 해당 위치 외 JSP 원문은 보존한다.
- 완성용 복사 문서도 동일한 template data 유형과 view provider 구성을 사용한다.
- 미완성 JSP는 현재 lexer 규칙에 따라 서버 영역으로 남으며 예외를 내지 않는다.
- JS·CSS 기능은 필수 플러그인 의존성이 아니다.
