# 조사: JSP 텍스트 에디터

## 결정과 근거

- FileType 직접 구현: 현재 범위에 언어 분석이 없고 비바이너리 유형은 기본 TextEditorProvider로 열린다. 인코딩은 기본값을 사용해 IDE 설정에 맡긴다.
- 대안인 LanguageFileType과 Language는 향후 파싱을 추가할 때 검토한다. 별도 FileEditorProvider는 현재 필요하지 않다.
- LSP를 사용하지 않으므로 필수 의존성을 com.intellij.modules.platform으로 줄인다. LSP 자체는 2025.3 무료 기능에서도 제공되므로 유료 여부 때문에 제거하는 것은 아니다.
- 기존 IntelliJ IDEA 2025.3.6.1 대상을 유지한다. 이전 Community 버전까지 지원 범위를 확장하지 않는다.
- 사용자가 정한 파일 연결을 강제로 바꾸는 코드는 추가하지 않는다.

## 공식 자료

- [파일 유형 등록](https://plugins.jetbrains.com/docs/intellij/registering-file-type.html)
- [FileType 소스](https://github.com/JetBrains/intellij-community/blob/master/platform/core-api/src/com/intellij/openapi/fileTypes/FileType.java)
- [텍스트 에디터 제공자](https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/openapi/fileEditor/impl/text/TextEditorProvider.kt)
- [무료 LSP 제공 변경](https://plugins.jetbrains.com/docs/intellij/api-notable-list-2025.html)

미해결 기술 결정은 없다.
