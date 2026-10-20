# 구현 계획: 선언부와 함수 포매팅

**작성일**: 2026-10-20 | **명세**: [spec.md](spec.md)

## 요약

선언부 호스트와 잎 블록의 추가 들여쓰기를 제한하고, 독립된 여러 줄 선언부 전체를 정렬할 때 Java 멤버와 종료 구분자를 함께 정렬한다.

## 기술 문맥

Kotlin/JVM IntelliJ 플러그인이며 로컬 261 SDK를 사용한다. 기존 Java 주입 문맥과 Java 포매터를 재사용한다. 영구 상태·새 의존성·네트워크 처리는 없다. BasePlatformTestCase와 로컬 Gradle 검증을 사용한다. 사용자 선택 영역을 넘지 않고 원문 토큰을 보존해야 한다.

## 헌장 점검

헌장은 미작성 템플릿이므로 원칙 검증을 생략한다. 저장소의 한글 문서·speckit 단계 지침을 준수하며 설계 전후 충돌이 없다.

## 설계와 파일 구조

- `src/main/kotlin/com/look4counter/jsp/JspFormattingModelBuilder.kt`: DECLARATION 잎과 선언 JAVA_HOST에 지시자와 같은 HTML 부모 들여쓰기 정책을 적용한다. 다른 Java 호스트 정책은 유지한다.
- `src/main/kotlin/com/look4counter/jsp/JspJavaFormatting.kt`: 기존 Java 복사본 포매팅 로직을 공유하고 전체가 선택된 독립 여러 줄 선언부는 HTML 포매팅 후 처리한다. 확정된 호스트 위치를 기준으로 Java 들여쓰기와 시작·종료 경계 공백을 함께 조정한다. 전처리의 허용 범위 마커로 선택을 보존한다. HTML 블록이 없는 최상위에서는 HTML PSI 구조와 허용 범위를 확인해 기존 선행 공백도 정렬한다. 한 줄·부분 선택·다른 Java는 기존 전처리 경로를 유지한다. 문자열·주석은 Java 렉서로 보호한다.
- `src/main/resources/META-INF/plugin.xml`: 선언부 후처리 포매터를 등록한다.
- `src/test/kotlin/com/look4counter/jsp/JspDeclarationFormattingTest.kt`: 사용자 예시, HTML 중첩, 함수 스타일·탭, 여러 선언부와 원문·선택 영역·반복 실행을 검증한다.
- `README.md`: 선언부 구분자와 본문 정렬 정책을 설명한다.

문서는 현재 기능 디렉터리의 명세·연구·데이터 모델·편집 계약·검증 안내·작업 목록을 사용한다.

## 위험과 검증

Java 가상 클래스의 들여쓰기를 문서로 그대로 투영하면 어긋난다. 복사본의 기준 들여쓰기를 제거한 후 실제 선언부 위치와 Java 설정을 적용한다. 전체 선언부를 선택하지 않으면 경계 공백을 변경하지 않는다. 기존 Java/HTML/JS/CSS 및 지시자 회귀를 모두 실행하고 배포 ZIP과 설정을 검증한다.
