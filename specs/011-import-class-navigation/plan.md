# 구현 계획: import 클래스 선언 이동

**명세**: [spec.md](spec.md)

## 기술 문맥과 요약

Kotlin/JVM과 IntelliJ IDEA 2026.1.3의 기존 JavaPsiFacade·PSI 참조 탐색을 사용한다. 새 의존성이나 영구 상태는 없다. JspDirective.getReferences에 page import 참조를 추가하며 기존 include 참조와 GotoDeclarationHandler는 유지한다.

## 설계

- src/main/kotlin/com/look4counter/jsp/JspDirective.kt: 파싱된 속성 목록을 유지해 반복 import도 처리하고, 쉼표 분리 항목의 공백을 제외한 범위에 참조를 제공한다. 기존 속성 맵은 호환성을 유지한다.
- 각 참조는 JavaPsiFacade.findClass(name, resolveScope)로 프로젝트·라이브러리·중첩 클래스를 찾는다. 인덱싱 중 null을 반환한다.
- 닫힌 page 지시자의 유효한 명시적 클래스 이름만 대상으로 하며 와일드카드·불완전 이름은 생략한다.
- src/test/kotlin/com/look4counter/jsp/JspImportNavigationTest.kt: 실제 GotoDeclarationAction 경로와 참조 범위·비대상·라이브러리·인덱싱·원문 보존을 검증한다.
- README.md: import에서 Ctrl+클릭·Ctrl+B로 이동하는 사용법을 추가한다.

## 헌장 점검

헌장은 미작성 템플릿이며 한글 문서·speckit 단계·기존 변경 보존을 준수한다. 설계 전후 위반 없음.

## 설계 산출물

[연구](research.md), [데이터 모델](data-model.md), [편집 계약](contracts/editor.md), [검증 안내](quickstart.md).