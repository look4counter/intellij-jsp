# 조사: JSP Java 영역 배경

## 결정과 근거

기존 선언부에서 검증한 `InjectionBackgroundSuppressor`를 공통 `JspJavaInjectionHost`로 이동한다. 플랫폼의 `InjectedLanguageUtil.isHighlightInjectionBackground`는 이 마커를 통해 배경만 억제하므로 Java 주입과 구문 강조는 유지된다. 스크립틀릿과 표현식에 배경이 남은 원인은 선언부 전용 타입에만 마커를 적용한 것이다.

## 대안

전역 색상 변경은 다른 파일에 영향을 주므로 제외한다. 세 종류별 하위 클래스를 만드는 방식은 동일한 정책을 중복하므로 공통 호스트 구현을 선택한다. 새 기술이나 미확정 조사 항목은 없다.
