<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ tag body-content="scriptless" %>
<%@ attribute name="title" required="true" %>
<head>
    <title>${title}</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="user-scalable=no, width=device-width, initial-scale=1.0"/>
    <link rel="stylesheet" href="<c:url value="/index.css"/>">
    <style id="user-style"></style>
    <script src="<c:url value="/index.js"/>"></script>
    <jsp:doBody/>
</head>