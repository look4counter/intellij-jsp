# 빠른 검증: JSP 자바 편집

## 자동 검증

`./gradlew.bat test buildPlugin verifyPluginProjectConfiguration`을 실행한다. 설치 IDE와 로컬 호환 테스트 도구 인자는 [이전 재현 명령](../004-jsp-formatting/quickstart.md)을 따른다.

## 수동 검증

1. Java가 활성화된 IDEA에 플러그인 ZIP을 설치한다.
2. 문자열 변수를 선언한 스크립틀릿과 뒤 표현식에서 멤버 완성과 강조를 확인한다.
3. 선언 메서드 및 HTML로 분리된 if 블록을 재정렬한다.
4. page import와 프로젝트 서블릿 API를 추가하고 request의 완성을 확인한다.
5. 선택 및 반복 재정렬과 미완성 구문에서 원문 보존을 확인한다.

## 결과

2026-10-20, IntelliJ IDEA 2026.1.3 SDK로 검증했다.

- 전체 테스트 45개 통과, 실패·오류 0개: 기존 웹 테스트 30개와 신규 Java 통합 테스트 15개.
- 선언·스크립틀릿·표현식 완성, 지역 변수 공유, 선언 멤버, page import, 프로젝트 타입 및 jakarta request 멤버를 검증했다.
- HTML 속성 안의 완성 결과가 원본 JSP에 반영되고 한글과 주변 마크업이 보존됨을 확인했다.
- 세 종류 자바 영역의 키워드·문자열·주석과 여러 조각 사이의 즉시 강조를 실제 등록 강조기로 확인했다.
- Java 코드 스타일, 선언과 실행 조각의 재정렬, 가상 들여쓰기 제거, 전체·부분 호스트 선택 외부 보존, 문자열 원문과 반복 실행 안정성을 검증했다.
- 빈 영역과 미완성 구문의 원문 매핑 및 재정렬 중 문자 보존을 확인했다.
- `buildPlugin` 및 `verifyPluginProjectConfiguration` 성공. ZIP에는 Java 의존성과 주입기·강조기·전처리 포매터가 포함되고 테스트 호환 도구는 포함되지 않는다.

## 재현 명령

```powershell
$env:GRADLE_USER_HOME = "$PWD/.gradle-user-home"
$env:GRADLE_RO_DEP_CACHE = 'C:/Users/tcpip147/.gradle/caches'
.\.gradle-user-home/gradle-9.7.1/bin/gradle.bat test buildPlugin verifyPluginProjectConfiguration -PlocalIdePath=D:/Common/idea-2026.1.3.win '-PlocalTestFrameworkPath=D:/Project/intellij-jsp/.gradle-user-home/test-harness/test-framework-compat.jar' -PinstrumentCode=false -PtestWebLanguages=true '-Pkotlin.incremental=false' --offline --no-daemon --no-configuration-cache -I .gradle-user-home/local-test-cache.init.gradle.kts '-Porg.gradle.java.installations.paths=D:/Common/idea-2026.1.3.win/jbr,D:/Project/intellij-jsp/.gradle-user-home/caches/9.7.1/transforms/41d2ee0b381ac1ce59d15aa3eb4932b1/transformed/idea-2025.3.6.1-win/jbr'
```

## 환경 제한

외부 다운로드 제한으로 이전 기능의 로컬 테스트 호환 도구를 사용하고 검증 실행에서만 계측을 껐다. 제품 코드와 언어 엔진은 실제 261 SDK다. 호환 도구의 강조 데몬 호출은 제거된 API 때문에 실행할 수 없어 등록된 에디터 강조기를 직접 검증했다. 정식 261 테스트 프레임워크와 실제 IDE 화면에서의 수동 설치·단축키 검증은 수행하지 않았다.

설치 파일은 `build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip`이다.
