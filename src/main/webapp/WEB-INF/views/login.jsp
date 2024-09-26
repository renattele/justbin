<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<t:header title="Log in."/>
<body class="centered-container" style="gap: 20px">
<script>
    function login() {
        const username = document.getElementById("username").value
        const password = document.getElementById("password").value
        fetch("<c:url value="/login"/>", {
            method: 'POST',
            body: base64(username) + "\n" + base64(password)
        }).then(response => {
            const status = document.getElementById("status")
            if (response.status === 401) {
                status.innerHTML = "Incorrect";
            } else if (response.status === 200) {
                loginPersistent(username, password);
                navigateBack();
            } else {
                status.innerHTML = "Error";
            }
        })
    }

    function onInput() {
        document.getElementById("status").innerHTML = "";
    }
</script>
<h3 id="status"></h3>
<input id="username" oninput="onInput()" style="font-size: xx-large" placeholder="username">
<input id="password" oninput="onInput()" type="password" style="font-size: xx-large" placeholder="password">
<div style="display: flex; flex-direction: column; gap: 12px">
    <t:button primary="true" onclick="login()" style="margin-top: 40px; width: 200px">Log in</t:button>
    <a href="<c:url value="/signup"/>">
        <p>or sign up</p>
    </a>
</div>
</body>
</html>