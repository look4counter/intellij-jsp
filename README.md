# IntelliJ JSP 에디터

IntelliJ IDEA 2026.1 계열의 무료 기능 환경에서 `.jsp` 파일을 열고 편집·저장하는 플러그인입니다. 기본 빌드 대상은 2026.1.3이며 최소 지원 빌드는 261입니다.

JSP 파일 유형, 기본 텍스트 편집과 구문 강조를 제공합니다. 인코딩, 실행 취소, 읽기 전용과 저장 처리는 IDE의 기본 기능을 사용합니다.

프로젝트 트리와 에디터 탭에는 IntelliJ에서 제공하는 기본 JSP 아이콘(`AllIcons.FileTypes.Jsp`)을 표시합니다. 테마와 화면 배율 처리는 플랫폼 기본 동작을 따릅니다.

## 구문 강조

- HTML 태그·속성·문자열·주석은 기존 HTML 강조기와 색상 설정을 사용합니다.
- JSP 지시자(`<%@ %>`), 스크립틀릿(`<% %>`), 표현식(`<%= %>`), 선언(`<%! %>`), 주석(`<%-- --%>`) 및 EL(`${…}`, `#{…}`)을 구분합니다.
- 활성화된 JavaScript·CSS 지원이 있으면 `<script>`·`<style>` 내부 강조를 자동으로 재사용합니다. 해당 기능이 없는 환경에서도 HTML·JSP 강조와 편집은 사용할 수 있습니다.
- HTML 속성이나 JS 문자열·CSS 값 안의 JSP를 분리하면서 주변 언어의 강조 문맥을 이어갑니다.
- 미완성 구문과 편집 중 구분자 변경을 처리하며 IDE의 색상 테마를 따릅니다.

## 자동완성

JSP의 HTML 태그·속성과 활성화된 기존 JS·CSS 지원의 자동완성을 재사용합니다. `<script>` 내부의 지역 변수, `<style>` 내부의 CSS 속성 위치에서 기본 완성을 사용할 수 있습니다. JSP 전용 영역은 HTML 구문 트리와 분리하고 원본 위치를 유지합니다.

스크립틀릿(`<% %>`), 표현식(`<%= %>`), 선언(`<%! %>`)은 기존 Java 엔진의 자동완성과 구문강조를 사용합니다. 같은 파일의 지역 변수·선언 멤버와 `page import`, 프로젝트 클래스 문맥을 공유하며 HTML로 나뉜 제어문도 연결합니다.

Java 플러그인이 필요합니다. `request`, `response`, `session`, `application`, `config`, `pageContext`, `out`의 멤버 완성은 프로젝트에 등록된 서블릿·JSP API를 사용합니다. jakarta API가 있으면 우선하고, 없으면 javax API를 사용합니다. 라이브러리가 없으면 해당 타입은 미해결 상태가 됩니다. EL·태그 라이브러리의 전용 자동완성과 JSP 실행은 후속 개발 범위입니다.

## 정적 include 문맥

`<%@ include file="parts/common.jspf" %>`로 연결한 파일의 import·선언·Java 실행 문맥을 공유합니다. 중첩 include는 삽입 위치에서 펼치므로 지역 변수와 제어문의 순서·범위를 유지합니다. `.jspf`도 JSP 에디터로 편집합니다.

조각을 포함하는 부모 경로가 하나이면 조각 편집에도 부모 문맥을 제공합니다. 여러 부모가 있거나 같은 조각을 여러 번 포함하면 조각 자체 문맥만 사용합니다. 완성 선택과 포매팅은 현재 파일만 수정합니다.

상대경로는 지시자가 있는 파일 기준으로 해석합니다. `/`로 시작하면 현재 콘텐츠 루트 안의 가장 가까운 `webapp`·`WebContent`·`web` 디렉터리 또는 `WEB-INF`를 가진 상위 디렉터리를 웹 루트로 사용하고, 없으면 콘텐츠 루트를 사용합니다. 다른 모듈의 웹 루트를 임의로 선택하지 않습니다.

포함 파일과 지시자의 커밋된 미저장 편집 및 파일 생성·삭제를 반영합니다. 순환과 누락은 건너뛰며 깊이 64, 확장 1024회, 확장 원문 2백만 문자로 제한합니다. 인덱싱 중에는 부모 역탐색을 생략합니다. 요청 시점의 `<jsp:include>`, XML JSP, `web.xml`의 prelude/coda와 EL·태그 문맥은 이 기능에 포함하지 않습니다.

## 선언 이동과 지시자 완성

정적 include의 `file` 값에서 **Ctrl+클릭** 또는 **Ctrl+B**로 대상 파일을 엽니다. 경로 해석은 위 include 문맥 정책을 따릅니다.

JSP의 Java 함수 호출에서도 같은 조작으로 실제 JSP·JSPF 선언의 함수 이름 위치로 이동합니다. 중첩 include와 같은 파일의 선언을 지원하며 오버로드는 Java 해석 결과를 따릅니다. 포함된 선언을 편집하면 이동 위치도 갱신됩니다. 일반 Java 클래스 메서드 이동은 플랫폼에 위임합니다.

`<%@` 다음에는 `page`, `include`, `taglib`를 완성합니다. 각 지시자에 맞는 표준 속성과 boolean·language·buffer·pageEncoding·contentType의 대표 값을 제공합니다. 속성 선택은 필요한 `=""`를 삽입하고 값 입력 위치로 커서를 옮깁니다. 이미 작성한 속성은 반복 가능한 `page import`를 제외하고 중복 제안하지 않습니다. Ctrl+Space로 완성을 호출할 수 있습니다.

tag 파일 전용 지시자, TLD 검색·태그 완성과 이름 변경 리팩터링은 이 기능의 범위에 포함하지 않습니다.

## Java import 자동 추가

JSP·JSPF의 스크립틀릿·표현식·선언에서 Ctrl+Space로 Java 클래스를 선택하면 현재 파일에 필요한 `<%@ page import="패키지.클래스" %>` 지시자를 추가합니다. 미해결 클래스 이름에서는 Alt+Enter의 `JSP import 추가: 패키지.클래스`를 선택합니다. 동명 클래스는 패키지를 확인해 선택할 수 있습니다.

기존 명시적·와일드카드·include import 및 `java.lang` 해석을 재사용합니다. 이름 충돌 시 완전한 클래스 이름을 사용하며 include 부모 파일은 수정하지 않습니다. HTML·EL·문자열·주석은 대상에서 제외합니다. 정적 import와 import 정리·삭제는 지원하지 않습니다.

`<%@ page import="demo.Widget, demo.Other" %>`에서 클래스 이름을 **Ctrl+클릭**하거나 **Ctrl+B**를 누르면 해당 Java 클래스로 이동합니다. JSP·JSPF의 쉼표 목록·반복 import 속성을 지원하며 프로젝트 클래스와 등록된 라이브러리·중첩 클래스는 기존 Java 탐색 정책을 따릅니다. 와일드카드와 찾을 수 없는 클래스에는 클래스 이동을 제공하지 않습니다.

## 코드 재정렬

JSP 에디터에서 **Ctrl+Alt+L**로 코드 재정렬을 실행합니다. 전체 파일과 선택 영역에 기존 HTML 및 활성화된 JavaScript·CSS 포매터와 코드 스타일 설정을 적용합니다.

자바 본문에는 기존 Java 포매터와 코드 스타일 설정을 적용합니다. JSP 지시자·주석 및 EL은 내부 공백과 줄바꿈을 보존합니다. JS·CSS 지원이 없는 환경에서도 HTML 재정렬을 사용할 수 있습니다.

독립된 줄의 `page`·`include`·`taglib` 지시자에서 `%>` 뒤 Enter를 누르면 시작 줄의 공백·탭 들여쓰기를 그대로 유지합니다. 최상위 지시자 뒤에는 추가 들여쓰기를 넣지 않습니다. HTML·Java 본문과 지시자 내부 Enter는 기존 처리를 사용합니다.

## 빌드와 실행

프로젝트의 Gradle 래퍼와 대상 IDE에 필요한 JDK 환경에서 실행합니다.

```powershell
.\gradlew.bat buildPlugin verifyPluginProjectConfiguration
.\gradlew.bat test
.\gradlew.bat test -PtestWebLanguages=true
.\gradlew.bat runIde
```

설치된 IDE SDK로 검증하려면 `-PlocalIdePath=D:/Common/idea-2026.1.3.win`을 추가할 수 있습니다. 개인 경로는 기본 빌드 설정에 포함하지 않습니다.

## 설치와 사용

1. `build/distributions/`에 생성된 ZIP을 준비합니다.
2. IntelliJ 설정 → 플러그인 → 톱니바퀴 → 디스크에서 플러그인 설치에서 ZIP을 선택합니다.
3. IDE를 재시작하고 프로젝트의 `.jsp` 파일을 더블 클릭합니다.
4. 기본 텍스트 에디터에서 수정하고 저장합니다.

기존 사용자 연결 때문에 파일 유형이 다르면 설정 → 에디터 → 파일 유형에서 `Community JSP`의 `*.jsp` 연결을 확인합니다. 플러그인은 사용자 연결을 강제로 바꾸지 않습니다.

2026.1 이전 버전은 현재 설치 대상에 포함하지 않습니다.

## 설계와 검증

- [기능 명세](specs/001-open-jsp-editor/spec.md)
- [구현 계획](specs/001-open-jsp-editor/plan.md)
- [검증 절차와 결과](specs/001-open-jsp-editor/quickstart.md)
- [구문 강조 명세](specs/002-jsp-syntax-highlighting/spec.md)
- [구문 강조 검증 절차와 결과](specs/002-jsp-syntax-highlighting/quickstart.md)
- [2026.1 자동완성 명세](specs/003-web-completion-2026/spec.md)
- [2026.1 자동완성 검증 결과](specs/003-web-completion-2026/quickstart.md)
- [JSP 포매팅 명세](specs/004-jsp-formatting/spec.md)
- [JSP 포매팅 검증 절차와 결과](specs/004-jsp-formatting/quickstart.md)
- [자바 편집 명세](specs/005-java-editor/spec.md)
- [자바 편집 검증 절차와 결과](specs/005-java-editor/quickstart.md)
- [include 문맥 명세](specs/007-include-context/spec.md)
- [include 문맥 검증](specs/007-include-context/quickstart.md)
- [선언 이동과 지시자 완성 명세](specs/008-navigation-directives/spec.md)
- [선언 이동과 지시자 완성 검증](specs/008-navigation-directives/quickstart.md)
- [Java import 자동 추가 명세](specs/009-java-auto-import/spec.md)
- [Java import 자동 추가 검증](specs/009-java-auto-import/quickstart.md)
- [import 클래스 이동 명세](specs/011-import-class-navigation/spec.md)
- [import 클래스 이동 검증](specs/011-import-class-navigation/quickstart.md)
- [지시자 Enter 들여쓰기 명세](specs/012-directive-enter-indent/spec.md)
- [지시자 Enter 검증](specs/012-directive-enter-indent/quickstart.md)
- [JetBrains 파일 유형 등록 문서](https://plugins.jetbrains.com/docs/intellij/registering-file-type.html)

