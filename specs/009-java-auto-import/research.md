# 연구: Java import 연결

- 결정: Java 완성 기여자 앞에서 남은 후보를 수집하고 클래스 후보 삽입만 장식한다. 근거: 기존 Java 후보·정렬·렌더링 재사용. 대안: 클래스 완성을 새로 구현하면 문맥과 순위를 중복 구현한다.
- 결정: 기본 Java 클래스 삽입기의 import 쓰기를 호출하지 않고 호스트 JSP 지시자를 추가한다. 근거: 로컬 261 SDK 바이트코드와 계획 단계 조사 에이전트 확인 결과 기본 동작은 주입 Java의 가상 import 목록을 수정한다. 대안: 기본 삽입 후 복구는 오프셋과 원문 보존이 불안정하다.
- 결정: ConstructorInsertHandler의 공개 괄호 삽입을 재사용한다. 근거: 생성자 완성 동작을 유지한다.
- 결정: UnresolvedReferenceQuickFixProvider에서 클래스별 빠른 수정을 등록한다. 근거: 패키지별 후보를 Alt+Enter에서 직접 선택한다. 후보 필터에는 기존 ImportClassFix의 탐색을 재사용한다.
- 결정: 별도 page import를 추가하고 기존 지시자를 보존한다. 근거: 따옴표·주석·속성 재작성이 불필요하다.

## 확인 자료

로컬 IntelliJ 261 JAR의 CompletionContributor, LookupElementDecorator, InsertionContext, ConstructorInsertHandler, ImportClassFix, UnresolvedReferenceQuickFixProvider API를 확인했다.
[공식 코드 완성 문서](https://plugins.jetbrains.com/docs/intellij/code-completion.html)의 기여자와 선택 후 삽입 계약을 따른다.
