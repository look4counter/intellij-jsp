# 연구: 지시자 표시와 완성

- 결정: 기존 Java 레이어와 같은 경계 재분리 방식으로 지시자 레이어를 추가한다. 연구 에이전트가 같은 종류 토큰의 레이어 연결과 미완성 문자열 상태 누출 위험을 확인했다. 기본 PSI lexer를 쪼개면 탐색·include 구조가 바뀌므로 제외한다.
- 결정: 기존 값 문맥에서 import·include를 추가한다. 현재 구현은 값 전체를 prefix로 사용하므로 현재 항목별 prefix와 삽입 범위를 명시해야 한다.
- 결정: JavaPsiFacade·PsiShortNamesCache와 파일 해석 범위로 클래스·패키지를 찾는다. 취소 점검과 인덱싱 중 생략을 적용한다.
- 결정: 기존 include 해석의 웹 루트·상대 경로·경계 검사를 재사용하는 디렉터리 탐색을 제공한다. VFS 자식만 열거하며 파일 확장자를 JSP로 제한하지 않는다.
- 결정: TypedHandlerDelegate.checkAutoPopup과 AutoPopupController로 지시자 구분 문자·속성 삽입 뒤 팝업을 지원한다. 현재 SDK 바이트코드에서 기본 CompletionAutoPopupHandler가 구형 contributor.invokeAutoPopup을 호출하지 않음을 추가 확인해 typedHandler를 선택했다. 다른 문맥은 기존 입력을 따른다.
