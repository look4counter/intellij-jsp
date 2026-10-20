# 연구: 지시자 블록 들여쓰기

- 연구 에이전트와 로컬 SDK 바이트코드를 확인했다. TemplateLanguageBlock은 getIndent를 재정의하지 않고 AbstractBlock.getIndent의 null을 상속한다. HTML 데이터 래퍼는 기존 HTML 블록의 들여쓰기를 따른다.
- 결정: 지시자 호스트·leaf의 추가 들여쓰기를 명시적으로 없앤다. HTML 부모 블록과 구조 병합은 유지한다.
- 제외한 대안: Enter 처리만 수정하는 것은 재정렬 경로에 적용되지 않는다. 문서 후처리로 선행 공백을 삭제하면 선택 영역과 HTML 중첩을 훼손할 수 있어 사용하지 않는다.
- 실제 CodeStyleManager 재정렬로 사용자 예시의 수정 전후와 반복 실행을 확인한다.

## 재현과 설계 보완

수정 전 사용자 예시에서 두 번째·세 번째 지시자 앞 공백 8칸이 정확히 재현됐다. 단순 NONE은 최상위를 고치지만 HTML 내부에서 한 단계 부족해졌다. 따라서 호스트는 직접 HTML 부모 원본의 getChildAttributes를 원본 자식 인덱스로 호출한다. 일반 들여쓰기는 HTML 코드 스타일의 크기로 변환하며 지시자 leaf에는 추가 들여쓰기를 넣지 않는다. SDK XmlTagBlock은 태그별 자식 들여쓰기 정책을, 파일·문서 XmlBlock은 NONE을 반환한다.
