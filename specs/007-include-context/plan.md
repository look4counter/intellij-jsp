# 구현 계획: JSP include 문맥

**작성일**: 2026-10-20 | **명세**: [spec.md](spec.md)

## 요약

정적 include를 원래 위치에서 펼친 번역 단위를 만들고 현재 파일의 자바 호스트만 실제 주입 범위로 연결한다. 다른 파일의 코드는 가상 접두·접미 코드로 제공하여 파일 간 편집을 방지한다. 선언과 실행 코드의 기존 분리 주입을 유지한다.

## 기술 문맥

Kotlin/JVM과 IntelliJ 2026.1.3 SDK, 기존 Java 플러그인을 사용한다. 추가 외부 의존성은 없다. FileTypeIndex로 프로젝트의 역방향 포함 관계를 만들고 CachedValuesManager로 PSI·VFS 구조·프로젝트 루트·인덱싱 상태 변경에 따라 무효화한다. 파일 내용은 현재 PSI를 사용한다. 인덱싱 중에는 역인덱스를 사용하지 않는다.

## 헌장 점검

헌장은 미작성 템플릿이므로 규범 검사는 생략한다. 한글 문서와 speckit 순서를 준수한다. 설계 전후 위반 없음.

## 연구 및 설계

[연구](research.md), [모델](data-model.md), [계약](contracts/include-context.md), [검증](quickstart.md)을 따른다.

## 구조와 변경 파일

- src/main/kotlin/com/look4counter/jsp/JspIncludeContext.kt: 지시자 분석, 경로 해석, 포함 그래프와 유일 부모 선택, 번역 단위 확장.
- src/main/kotlin/com/look4counter/jsp/JspJavaInjector.kt: 전체 import 및 선언, 삽입 위치의 외부 실행 코드를 주입 affix에 연결.
- src/main/resources/META-INF/plugin.xml: jspf 확장자 등록.
- src/test/kotlin/com/look4counter/jsp/JspIncludeContextTest.kt: 양방향 완성, 범위, 경로·변경·오류·편집 보존 통합 검증.
- README.md: 정적 include 지원 및 다중 부모·웹 루트 정책 안내.

## 구현 단계와 제한

1. 지시자를 기존 JSP 렉서로 구분하여 주석·동적 include와 분리한다.
2. 프로젝트 단위 역그래프와 유일 부모 체인을 구하고 대상이 번역 단위에 여러 번 나타나면 자체 문맥으로 되돌린다.
3. 깊이 64, 포함 확장 1024회, 확장 원문 합계 2백만 문자로 제한한다. 외부 파일이 2백만 바이트를 넘으면 읽기 전에 생략한다. 현재 파일의 로컬 코드는 크기 제한으로 제거하지 않는다. 순환 경로는 활성 방문 집합으로 중단하고 현재 파일이 부모 확장에서 누락되면 자체 문맥으로 복구한다.
4. 현재 파일의 호스트만 등록하여 원문 위치와 포매팅의 안전성을 유지한다.
5. 실제 완성과 편집 보존을 검증하고 기존 전체 테스트·빌드 후 수렴 점검한다.

## 후속 버그 재현 계획

사용자가 제공한 WebContent의 HTML 페이지와 include.jsp를 기반으로 test 메서드의 반환값 표현식 완성, void 스크립틀릿 완성 및 void 출력 표현식의 Java 후보 필터링을 비교한다. 재현 테스트는 JspIncludeContextTest.kt에 추가하고, 정상 호출 위치에서 실패하면 JspJavaInjector.kt와 JspIncludeContext.kt 중 원인이 있는 지점만 수정한다. 수정이 불필요하면 검증 결과와 정확한 사용 예를 기록한다.
