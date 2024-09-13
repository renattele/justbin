<jsp:useBean id="owner" scope="request" type="java.lang.String"/>
<jsp:useBean id="theme" scope="request" type="jbin.domain.Theme"/>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<t:header title="Theme.">
    <style>
        * {
            transition: all 0.3s;
        }
    </style>
    <script>
        function isValidColor(strColor) {
            return /^#([0-9A-F]{3}){1,2}$/i.test(strColor)
        }

        let oldCss = "";

        function editable() {
            return "${owner}" === getUsername();
        }

        function updateLocalTheme() {
            const background = document.getElementById("background").value
            const foreground = document.getElementById("foreground").value
            const css = document.getElementById("css").value
            if (isValidColor(background) && isValidColor(foreground)) {
                oldCss = cssFrom(background, foreground, css);
            } else {
                oldCss = cssFrom("#000000", "#ffffff", "")
            }
            loadTheme(oldCss);
        }

        function updateTextArea(element) {
            element.style.textAlign = element.innerHTML.length > 50 ? "start" : "center"
            element.style.height = "1px"
            element.style.height = element.scrollHeight + "px"
            element.style.fontSize = (100 - element.innerHTML.length).clamp(14, 32) + "px"
        }

        function onLoad() {
            const isEditable = editable();
            const inputs = document.getElementsByTagName("input")
            for (let i = 0; i < inputs.length; i++) {
                inputs[i].readOnly = !isEditable;
            }
            document.getElementsByTagName("textarea")[0].readOnly = !isEditable;
            if (isEditable) {
                const deleteButton = `<t:button onclick="deleteTheme()" style="width: 200px">Delete</t:button>`
                document.getElementById("actions").innerHTML += deleteButton
            }
        }

        function update(path, body = null) {
            return fetchWithAuth("/t/${theme.id()}/" + path, {
                method: "POST",
                body: body
            })
        }

        function applyTheme() {
            setTheme(cssFrom(document.getElementById("background").value,
                document.getElementById("foreground").value,
                document.getElementById("css").value))
            navigateBack()
        }

        function deleteTheme() {
            return update("delete").then(response => {
                if (response.status === 200) {
                    navigateBack();
                }
            })
        }

        const updateAll = debounce(1000, () => {
            const name = document.getElementById("name").value
            const background = document.getElementById("background").value
            const foreground = document.getElementById("foreground").value
            const css = document.getElementById("css").value
            update("edit", base64(name) + "\n" + base64(background) + "\n" + base64(foreground) + "\n" + base64(css))
        })
    </script>
</t:header>
<body onload="onLoad()" onpageshow="updateLocalTheme()" class="centered-container" style="gap: 20px">
<input id="name" placeholder="Edit me" value="<c:out value="${theme.name()}"/>" oninput="updateAll()">
<c:if test="${not empty owner}">
    <p style="margin-bottom: 40px; margin-top: -12px">by ${owner}</p>
</c:if>
<input id="foreground" value="<c:out value="${theme.foregroundColor()}"/>" placeholder="foreground color"
       style="font-size: xx-large"
       oninput="updateLocalTheme(); updateAll()" autocomplete="off">
<input id="background" value="<c:out value="${theme.backgroundColor()}"/>" placeholder="background color"
       style="font-size: xx-large"
       oninput="updateLocalTheme(); updateAll()" autocomplete="off">
<textarea
        id="css"
        placeholder="css"
        style="font-size: xx-large; text-align: center; height: auto"
        oninput="updateTextArea(this); updateLocalTheme(); updateAll()"
        autocomplete="off"><c:out
        value="${theme.css()}"/></textarea>
<script>
    updateTextArea(document.getElementById("css"))
</script>
<div id="actions" style="display: flex; flex-direction: row; gap: 20px">
    <t:button onclick="applyTheme()" style="width: 200px" primary="true">Apply</t:button>
</div>
</body>
</html>