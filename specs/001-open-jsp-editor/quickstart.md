# 빠른 검증

## 실행

```powershell
.\gradlew.bat buildPlugin verifyPluginProjectConfiguration
.\gradlew.bat runIde
```

생성된 build/distributions의 ZIP을 IDE 설정 → 플러그인 → 톱니바퀴 → 디스크에서 플러그인 설치로 설치한다.

## 수용 시나리오

1. 무료 기능만 사용하는 IDE에서 다음 index.jsp를 열어 JSP 텍스트 유형과 에디터 표시를 확인한다.

```jsp
<%@ page contentType="text/html; charset=UTF-8" %>
<h1>안녕하세요</h1>
<% String greeting = "JSP"; %>
<%= greeting %>
```

2. 한글을 수정하고 저장한 뒤 닫고 다시 열어 의도한 수정 외 원문 손실이 없는지 확인한다.
3. 빈 empty.jsp를 열어 입력·저장한다.
4. 읽기 전용 JSP는 기본 IDE와 동일하게 편집이 제한되는지 확인한다.
5. .txt, .java, .html 파일 연결이 유지되는지 확인한다.
6. 사용자가 *.jsp를 다른 유형에 연결한 경우 그 연결이 유지되는지 확인한다. JSP 유형 사용을 원하면 에디터 → 파일 유형에서 연결을 조정한다.

## 검증 기록

구현 후 실제 수행 결과와 수동 검증 미수행 여부를 구분해 기록한다.

## 2026-10-19 검증 결과

- `buildPlugin verifyPluginProjectConfiguration`: 성공. Kotlin 컴파일과 플러그인 ZIP 생성 완료.
- plugin.xml의 JSP 등록 이름, jsp 확장자, INSTANCE 필드 및 플랫폼 기본 의존성 검사: 성공.
- 제한된 환경에서는 작업 폴더에 Gradle 배포본과 쓰기 가능한 캐시를 두고 기존 의존성 캐시를 읽기 전용으로 사용했다. 대상 IDE의 JBR 21 경로를 지정해 빌드했다.
- 실제 실행 명령은 다음과 같다. 설치 환경에서 일반 Gradle 래퍼와 JDK 21을 사용할 수 있으면 위의 기본 명령을 사용한다.

```powershell
$env:GRADLE_USER_HOME = "$PWD/.gradle-user-home"
$env:GRADLE_RO_DEP_CACHE = 'C:/Users/tcpip147/.gradle/caches'
.\.gradle-user-home/gradle-9.7.1/bin/gradle.bat buildPlugin verifyPluginProjectConfiguration --offline --no-daemon --no-configuration-cache '-Porg.gradle.java.installations.paths=D:/Project/intellij-jsp/.gradle-user-home/caches/9.7.1/transforms/41d2ee0b381ac1ce59d15aa3eb4932b1/transformed/idea-2025.3.6.1-win/jbr'
```

- 실제 IDE UI에서 열기·저장·재열기 수용 시나리오는 미수행이다. 기본 텍스트 에디터 선택과 저장은 플랫폼에 위임하며, 수동 확인 절차는 위에 제공했다.
- 설치 ZIP: `build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip`.
