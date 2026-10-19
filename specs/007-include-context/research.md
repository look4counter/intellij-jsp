# 연구: include와 Java 주입

- 결정: 정적 include만 번역 시점에 펼친다. 동적 jsp:include는 요청 시점의 별도 실행이므로 지역 변수 문맥을 합치지 않는다. [Jakarta Pages 명세](https://jakarta.ee/specifications/pages/3.1/jakarta-server-pages-spec-3.1)의 번역 단위와 include 의미를 따른다.
- 주입: 다른 파일의 PSI 호스트를 하나의 주입 파일에 등록하지 않는다. 현재 파일의 호스트는 실제 범위로, 나머지는 가상 코드로 제공하여 위치 매핑과 편집 보존을 유지한다.
- 캐시: 로컬 261 SDK의 InjectionResult는 호스트 stamp와 프로젝트 전역 PSI modification count를 사용한다. 포함 파일의 커밋된 편집은 재주입을 유발한다. 역그래프 캐시는 PSI modification count, VFS 구조, 프로젝트 루트와 DumbService modification tracker에 의존한다.
- 경로: 상대경로는 포함 지시자의 파일 기준으로 해석한다. 웹 루트는 명세의 디렉터리 규칙을 사용하고 다른 모듈로 확장하지 않는다.
- 부모: 사용자의 선택에 따라 유일한 경로만 자동 사용한다. 여러 부모와 반복 삽입은 조각 자체 문맥으로 처리한다.
- 대안: 모든 부모를 합치면 실제로 공존하지 않는 변수와 충돌이 발생한다. 모든 파일의 호스트 직접 주입은 플랫폼의 동일 호스트 파일 제약과 맞지 않는다.
