# 연구: IntelliJ 기본 JSP 아이콘

- 결정: AllIcons.FileTypes.Jsp를 직접 사용한다.
- 근거: 설치된 2026.1.3 SDK의 AllIcons.FileTypes에 Jsp와 Jspx 필드가 존재함을 javap로 확인했다.
- 이유: 사용자가 기본 JSP 아이콘을 선택했으며 플랫폼의 테마·배율 처리를 그대로 사용할 수 있다.
- 대안: 자체 SVG와 IconLoader는 별도 리소스 관리가 필요하므로 제거한다.
- 의존성: 기존 플랫폼 API만 사용한다.
