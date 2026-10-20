# 조사: 선언부 배경

## 결정

로컬 261 SDK의 `InjectedLanguageUtil.isHighlightInjectionBackground`를 확인했다. 호스트가 `InjectionBackgroundSuppressor`를 구현하면 주입 배경을 적용하지 않는다. `InjectedGeneralHighlightingPass`는 이를 배경에만 사용하며 Java 구문 및 의미 강조는 별도로 실행한다.

선언부에만 이 마커를 구현하는 호스트 하위 클래스를 생성한다. 기존 공통 호스트의 편집·범위·치환 로직을 그대로 상속한다.

## 근거와 대안

직접 배경색을 설정하는 코드는 없고 선언부가 Java 주입 호스트다. 전역 색상 변경은 다른 언어에 영향을 주므로 제외한다. 공통 호스트에 마커를 추가하면 스크립틀릿과 표현식까지 바뀌므로 제외한다. 별도 하이라이트 필터는 기존 플랫폼 배경 억제 기능보다 복잡하다. 새 의존성은 없다.
