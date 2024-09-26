<%--suppress JspAbsolutePathInspection --%>
<jsp:useBean id="readonly" scope="request" type="java.lang.Boolean"/>
<jsp:useBean id="content" scope="request" type="java.lang.String"/>
<jsp:useBean id="filename" scope="request" type="java.lang.String"/>
<jsp:useBean id="fileId" scope="request" type="java.lang.String"/>
<jsp:useBean id="contentType" scope="request" type="java.lang.String"/>
<jsp:useBean id="collectionId" scope="request" type="java.lang.String"/>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<t:header title="${filename}">
    <script>
        function status(status) {
            const statusId = document.getElementById("status");
            statusId.innerHTML = status;
        }

        function update(path, data = null) {
            return fetch("<c:url value="/v/${collectionId}/${fileId}/"/>" + path, {
                method: "POST",
                body: data
            })
        }

        function updateFilenameNow() {
            const filename = document.getElementById("filename").value;
            update("edit_name", filename).then(response => {
                status(response.status !== 200 ? "Error" : "");
            })
        }

        const updateFilename = debounce(1000, updateFilenameNow)

        function updateContentNow() {
            const content = document.getElementById("content").value;
            console.log(content);
            update("edit_content", content).then(response => {
                status(response.status !== 200 ? "Error" : "");
            })
        }

        const updateContent = debounce(1000, updateContentNow)

        function deleteFile() {
            update("delete").then(response => {
                const success = response.status === 200;
                status(success ? "" : "Error");
                if (success) {
                    navigateBack()
                }
            })
        }
    </script>
</t:header>
<body onpagehide="updateFilenameNow()" class="centered-container-scrollable" style="gap: 20px">
<h3 id="status"></h3>
<input onfocusout="updateFilename()" id="filename" placeholder="Filename" value="<c:out value="${filename}"/>">
<div style="display: flex; flex-direction: row; gap: 20px; align-items: stretch">
    <a href="<c:url value="/d/${fileId}/${filename}"/>" style="display: flex; align-items: stretch">
        <t:button primary="true" style="width: 200px">
            Download
        </t:button>
    </a>
    <t:button onclick="deleteFile()" style="width: 200px">Delete</t:button>
</div>
<c:if test="${contentType.startsWith('text')}">
    <textarea id="content" placeholder="content"
              onfocusout="updateContent()"
              contenteditable="${readonly}"
    >${content}</textarea>
</c:if>
<c:if test="${contentType.startsWith('image')}">
    <img id="image" alt="<c:out value="${filename}"/>" src="<c:url value="/d/${fileId}/${filename}"/>">
</c:if>
<c:if test="${contentType.startsWith('video')}">
<video src="<c:url value="/d/${fileId}/${filename}"/>" controls muted preload="auto">
    </c:if>
</body>
</html>