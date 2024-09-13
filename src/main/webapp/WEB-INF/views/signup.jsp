<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<t:header title="Sign up.">
    <script>
        function signup() {
            const username = document.getElementById("username").value
            const password = document.getElementById("password").value
            const passwordRepeat = document.getElementById("passwordRepeat").value
            const status = document.getElementById("status");
            if (password === passwordRepeat) {
                fetch("<c:url value="/signup"/>", {
                    method: "POST",
                    body: base64(username) + "\n" + base64(password)
                }).then(response => {
                    console.log(response)
                    if (response.status === 409) {
                        status.innerHTML = "User with this name exists"
                    } else if (response.status === 200) {
                        loginPersistent(username, password);
                        navigateBack();
                    } else {
                        status.innerHTML = "Error"
                    }
                })
            }
        }

        function checkPassword() {
            const password = document.getElementById("password").value
            const passwordRepeat = document.getElementById("passwordRepeat").value
            document.getElementById("signupButton").style.opacity = password === passwordRepeat ? 1 : 0;
        }
    </script>
</t:header>
<body class="centered-container" style="gap: 20px">
<h3 id="status"></h3>
<input id="username" style="font-size: xx-large" placeholder="username">
<input id="password" oninput="checkPassword()" type="password" style="font-size: xx-large" placeholder="password">
<input id="passwordRepeat" oninput="checkPassword()" type="password" style="font-size: xx-large"
       placeholder="password (repeat)">
<div style="display: flex; flex-direction: column; gap: 12px">
    <t:button id="signupButton" primary="true" onclick="signup()"
              style="margin-top: 40px; opacity: 0; width: 200px">Sign up</t:button>
    <a href="<c:url value="/login"/>">
        <p>or log in</p>
    </a>
</div>
</body>
</html>