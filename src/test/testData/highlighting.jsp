<%@ page contentType="text/html; charset=UTF-8" %>
<%! private String title = "JSP 에디터"; %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title><%= title %></title>
    <style>
        .card {
            color: red;
            margin: 12px;
            content: "앞${theme.name}뒤";
        }
    </style>
</head>
<body>
    <%-- <script>이 내용은 JSP 주석입니다.</script> --%>
    <% String greeting = "안녕하세요"; %>
    <h1 class="card ${theme.name}" title="앞<%= title %>뒤"><%= greeting %></h1>
    <p>${user.name} / #{bean.value}</p>
    <script>
        const message = "앞<%= greeting %>뒤";
        console.log(message, 42);
    </script>
</body>
</html>
