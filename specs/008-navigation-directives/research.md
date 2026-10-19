# 연구: 이동과 완성 확장

- include: 복합 지시자 PSI의 getReferences와 getReference에서 경로 값 참조를 반환한다. 기존 경로 해석을 공유한다. 사용자 언어 PSI는 참조 제공자를 등록하는 것만으로 참조가 생기지 않는다. [참조 해석 문서](https://plugins.jetbrains.com/docs/intellij/references-and-resolve.html).
- 함수 이동: GotoDeclarationHandler를 먼저 실행하고 Java resolve가 선택한 PsiMethod 이름의 오프셋을 매핑한다. 이름만 검색하면 오버로드를 잘못 선택할 수 있다. 로컬 261 SDK에서 핸들러가 일반 참조 fallback보다 먼저 실행됨을 확인했다.
- 매핑: 가상 접두·접미 코드는 일반 injectedToHost 변환으로 원본을 찾을 수 없다. 주입 생성 중 원본 범위를 기록하고 putInjectedFileUserData로 주입 파일에 저장한다. 변경 시 기존 주입 캐시 무효화에 따라 다시 만든다.
- 완성: CommunityJSP 전용 CompletionContributor에서 렉서로 지시자 위치를 확인하고 이름·속성·값 상태를 구분한다. [완성 문서](https://plugins.jetbrains.com/docs/intellij/code-completion.html).
- 표준 지시자: 일반 JSP의 page·include·taglib를 대상으로 한다. 속성은 [Jakarta Pages 명세](https://jakarta.ee/specifications/pages/3.1/jakarta-server-pages-spec-3.1)를 기준으로 한다.
- 검증: GotoDeclarationAction의 실제 대상 해석과 원본 파일·식별자 오프셋, 완성 선택 후 문서를 확인한다. IDE 화면에서의 수동 클릭 검증과 자동 해석 검증은 구분한다.
