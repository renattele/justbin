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
		<script>
			function updateCollections(parentHtml = "recents", onclick = null) {
						function click() {
								onclick()
						}
						let onclickValue = onclick != null ? "onclick=\"click()\"" : ""
            let recent = JSON.parse(localStorage.getItem("recent_collections"));
            let html = "";
            if (recent == null) recent = Array.of();
            if (recent.length !== 0) {
                html += `
                <div onclick="clearRecent()" style="cursor: pointer">
                    <p>clear</p>
                </div>
                `
            }
            recent.reverse()
            recent.forEach(collection => {
                html += `
            <t:button href="$href" $onclick style="width: 280px">$collection</t:button>
            `.replace("$collection", collection.name).replace("$href", "/c/" + collection.id)
						.replace("$onclick", onclickValue)
            })
            console.log(html)
            document.getElementById(parentHtml).innerHTML = html
        }
		</script>
    <jsp:doBody/>
</head>
