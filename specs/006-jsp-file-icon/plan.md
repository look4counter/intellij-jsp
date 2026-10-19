# 구현 계획: IntelliJ 기본 JSP 아이콘

**작성일**: 2026-10-20 | **명세**: [spec.md](spec.md)

## 요약

JspFileType.getIcon에서 AllIcons.FileTypes.Jsp를 직접 반환한다. 이전에 추가한 jsp.svg 및 jsp_dark.svg를 삭제하고 안내 문서를 갱신한다.

## 기술 문맥

기존 Kotlin과 IntelliJ IDEA 2026.1.3 SDK를 사용한다. javap로 플랫폼의 기본 JSP 아이콘 필드를 확인했다. 추가 의존성과 영속 데이터는 없다. 낮은 영향도의 연결 변경이므로 새 테스트는 작성하지 않고 빌드·설정 검증, 컴파일된 반환값 및 패키지 리소스를 확인한다.

## 헌장 점검

헌장은 미작성 템플릿이다. 한글 문서와 speckit 순서를 준수하며 설계 전후 위반 없음.

## 연구 및 설계

[연구](research.md), [모델](data-model.md), [계약](contracts/icon.md), [검증](quickstart.md)을 따른다.

## 파일 구성과 단계

1. src/main/kotlin/com/look4counter/jsp/JspFileType.kt에서 기본 아이콘을 직접 반환한다.
2. src/main/resources/icons/jsp.svg와 jsp_dark.svg를 삭제한다.
3. README.md의 안내를 갱신한다.
4. 빌드 및 배포물 검증 후 수렴 점검한다.
