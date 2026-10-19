<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <style>
        .card {
            content: "앞${user.name}뒤";
            /* 다음 줄에서 col을 입력하고 Ctrl+Space로 color를 선택합니다. */
        }
    </style>
</head>
<body>
    <%-- 이 주석 뒤에서도 HTML 완성이 동작해야 합니다. --%>
    <!-- 아래에서 <di를 입력하거나 div 안에서 cl을 입력하고 Ctrl+Space를 누릅니다. -->
    <div title="앞<%= title %>뒤">한글</div>
    <script>
        const greetingMessage = "앞<%= greeting %>뒤";
        // 다음 줄에서 greetingMes를 입력하고 Ctrl+Space로 greetingMessage를 선택합니다.
    </script>
</body>
</html>
