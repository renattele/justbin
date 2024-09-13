<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<t:header title="Themes.">
    <script>
        function updateTheme(background, foreground, css) {
            setTheme(cssFrom(background, foreground, css))
        }

        function load() {
            const options = document.getElementById("loginOptions")
            if (!isLoggedIn()) {
                document.getElementById("new-theme-button").style.display = "none";
                options.innerHTML = `
                <a style="flex: 1" href="<c:url value="/login"/>">
                    <p>log in</p>
                </a>
                <a style="flex: 1;" href="<c:url value="/signup"/>">
                    <p>sign up</p>
                </a>
                `;
            } else {
                options.innerHTML = `
                <div style="flex: 1; cursor: pointer" onclick="logout(); load()">
                    <p>log out</p>
                </a>
                `
            }
        }

        function createTheme() {
            fetchWithAuth("themes/create", {
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
    </script>
</t:header>
<body onpageshow="load()" class="centered-container-scrollable">
<div class="centered-container-scrollable" style="width: 100%">
    <div style="display: flex; flex-direction: column; margin-bottom: 50px">
        <h1 style="text-align: center; flex: 1; margin: 0">Themes.</h1>
        <div id="loginOptions" style="flex: 1; display: flex">

        </div>
    </div>
    <div id="content-container" class="content-container">
        <t:button id="new-theme-button" onclick="createTheme()" primary="true">New theme</t:button>
        <jsp:useBean id="themes" scope="request" type="java.util.List"/>
        <c:forEach items="${themes}" var="theme" varStatus="loop">
            <style>
                #theme-${loop.index} {
                    --background-color: ${theme.backgroundColor()};
                    --foreground-color: ${theme.foregroundColor()};
                }
            </style>
            <div id="theme-${loop.index}">
                <t:button
                        href="/t/${theme.id()}">
                    ${theme.name()}
                </t:button>
            </div>
        </c:forEach>
    </div>
</div>
</body>
</html>