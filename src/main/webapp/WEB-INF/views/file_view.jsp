<%--suppress JspAbsolutePathInspection --%>
<jsp:useBean id="content" scope="request" type="java.lang.String"/>
<jsp:useBean id="filename" scope="request" type="java.lang.String"/>
<jsp:useBean id="fileId" scope="request" type="java.lang.String"/>
<jsp:useBean id="contentType" scope="request" type="java.lang.String"/>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<t:header title="${filename}">
    <script>
        async function fetchCollections() {
            const collections = document.getElementById("collections")
            let recent = getRecent()
            let html = '';
            console.log(recent)
            for (const collection of recent) {
                let selected = false;
                const response = await fetch("/c/" + collection.id + "/raw");
                const text = await response.text()
                console.log(text);
                const ids = text.split("\n")
                selected = ids.some(value => value === "${fileId}")
                if (selected) {
                    html += `
                <t:button style="width: 200px" onclick="toggleCollection('$id')" primary="true">$name</t:button>
                `.replace("$id", collection.id).replace("$name", collection.name).replace("$primary", "");
                } else {
                    html += `
                <t:button style="width: 200px" onclick="toggleCollection('$id')">$name</t:button>
                `.replace("$id", collection.id).replace("$name", collection.name).replace("$primary", "");
                }
            }
            collections.innerHTML = html;
        }

        function toggleCollection(id) {
            fetch("/v/${fileId}/toggle_collection?q=" + id, {
                method: 'POST'
            }).then(response => {
                fetchCollections()
            })
        }
    </script>
</t:header>
<body onload="fetchCollections()" class="centered-container-scrollable" style="gap: 20px">
<h1><c:out value="${filename}"/></h1>
<div id="collections" style="display: flex; flex-direction: row; gap: 20px; align-items: stretch; width: 100%; flex-flow: row wrap; justify-content: center">
</div>
<div style="display: flex; flex-direction: row; gap: 20px; align-items: stretch">
    <a href="<c:url value="/d/${fileId}/${filename}"/>" style="display: flex; align-items: stretch">
        <t:button primary="true" style="width: 200px">
            Download
        </t:button>
    </a>
</div>
<embed src="<c:url value="/d/${fileId}/${filename}"/>" style="width: 100%; height: 100%">
</body>
</html>
