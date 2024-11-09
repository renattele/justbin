<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="themes" scope="request" type="java.util.List"/>
<jsp:useBean id="user" scope="request" type="java.lang.String"/>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<t:header title="Themes.">
    <script>
        function updateTheme(background, foreground, css) {
            setTheme(cssFrom(background, foreground, css))
        }

        function createTheme() {
            fetch("themes/create", {
                method: "POST"
            }).then(response => {
                if (response.status !== 200) {
                    return null;
                }
                return response.text();
            }).then(response => {
                if (typeof response === 'string') {
                    location.href = "/t/" + response;
                }
            })
        }

        async function logout() {
            await fetch("<c:url value="/logout"/>", { method: 'POST' })
            reload()
        }
    </script>
</t:header>
<body class="centered-container-scrollable">
<div class="centered-container-scrollable" style="width: 100%">
    <div style="display: flex; flex-direction: column; margin-bottom: 50px">
        <h1 style="text-align: center; flex: 1; margin: 0">Themes.</h1>
        <div id="loginOptions" style="flex: 1; display: flex">
            <c:if test="${empty user}">
                <a style="flex: 1" href="<c:url value="/login"/>">
                    <p>log in</p>
                </a>
                <a style="flex: 1;" href="<c:url value="/signup"/>">
                    <p>sign up</p>
                </a>
            </c:if>
            <c:if test="${not empty user}">
                <div style="flex: 1; cursor: pointer" onclick="logout()">
                    <p>log out</p>
                </div>
            </c:if>
        </div>
    </div>
    <div id="content-container" class="content-container">
        <c:if test="${not empty user}">
            <t:button id="new-theme-button" onclick="createTheme()" primary="true">New theme</t:button>
        </c:if>
        <c:forEach items="${themes}" var="theme" varStatus="loop">
            <style>
                #theme-${loop.index} {
                    --background-color: <c:out value="${theme.backgroundColor()}"/>;
                    --foreground-color: <c:out value="${theme.foregroundColor()}"/>;
                }
            </style>
            <div id="theme-${loop.index}">
                <t:button
                        href="/t/${theme.id()}">
                    <c:out value="${theme.name()}"/>
                </t:button>
            </div>
        </c:forEach>
    </div>
</div>
</body>
</html>