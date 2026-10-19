# 데이터 모델

- JSP 문서는 원문 하나를 공유하며 기본 JSP root와 HTML root를 가진다.
- JSP root의 HTML_TEXT는 outer 요소이다. JSP 토큰은 JSP 언어에 속한다.
- HTML root는 JSP 영역을 outer 요소로 보존해 전체 길이와 위치가 원본과 같다.
- script·style 내부는 기존 HTML parser가 연결한 웹 언어 구문 구조를 사용한다.
- 후보는 IDE가 생성하고 기존 삽입 핸들러가 원본 위치에 적용한다. 플러그인은 후보 상태를 별도로 저장하지 않는다.
- 문서 복사와 수정 시 view provider를 복제·동기화한다.
