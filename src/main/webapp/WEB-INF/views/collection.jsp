<jsp:useBean id="collectionName" scope="request" type="java.lang.String"/>
<jsp:useBean id="collectionID" scope="request" type="java.lang.String"/>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<html lang="en">
<t:header title="${collectionName}">
    <style>
        .binary-file-card-icon:hover .new-document-type {
            transition: all 0.3s;
            display: block;
        }

        .new-document-button {
            cursor: pointer;
        }

        .new-document-button:hover .new-document-nohover {
            display: none;
        }

        .new-document-button:hover .new-document-type {
            display: block;
        }

        .new-document-type {
            transition: all 0.3s;
            display: none;
        }

        .binary-file-card-name {
            position: absolute;
            top: auto;
            bottom: auto;
        }

        .binary-file-card-hint {
            position: absolute;
            bottom: 0;
            right: 4px;
            font-size: small;
        }
    </style>
    <script>
        reloadOnBack()

        function onLoad() {
            let recent = JSON.parse(localStorage.getItem("recent_collections"));
            if (recent == null) recent = Array.of();
            recent = recent.filter(c => c.id !== "<c:out value="${collectionID}"/>");
            recent.push({
                id: "<c:out value="${collectionID}"/>",
                name: document.getElementById("collection-name-input").value
            });
            localStorage.setItem("recent_collections", JSON.stringify(recent));
        }

        function changeNameNow() {
            onLoad()
            fetch("/c/<c:out value="${collectionID}"/>/edit_name", {
                method: "POST",
                body: document.getElementById("collection-name-input").value
            })
        }

        const changeName = debounce(1000, changeNameNow)

        function uploadBin() {
            const input = document.createElement("input");
            input.type = 'file';
            input.onchange = () => {
                const formData = new FormData();
                const files = input.files;
                for (let file of files) {
                    formData.append(file.name, file);
                }
                uploadFiles(formData);
            }
            input.click()
            input.remove()
        }

        function uploadFiles(formData) {
            fetch("/c/<c:out value="${collectionID}"/>/create_bin", {
                method: "POST",
                body: formData
            }).then(response => {
                location.reload()
            })
        }

        function loadDrag() {
            document.body.addEventListener("dragenter", (e) => {
                e.stopPropagation();
                e.preventDefault();
            }, false);
            document.body.addEventListener("dragover", (e) => {
                e.preventDefault();
            }, false);
            document.body.addEventListener("drop", (files) => {
                files.preventDefault();
                const formData = new FormData();
                for (let i = 0; i < files.dataTransfer.items.length; i++) {
                    const file = files.dataTransfer.items[i];
                    formData.append(file.filename, file.getAsFile());
                }
                uploadFiles(formData)
            })
        }
    </script>
</t:header>
<body onpagehide="changeNameNow()" onload="onLoad(); loadDrag()" class="centered-container">
<input onfocusout="changeName()" id="collection-name-input" style="width: 100%; margin-top: 50px" size="20"
       value="<c:out value="${collectionName}"/>" oninput="changeName()">
<div id="content-container" class="content-container" style="padding-top: 20px">
    <jsp:useBean id="files" scope="request" type="java.util.List"/>
    <c:forEach items="${files}" var="file">
        <t:button href="/v/${file.id()}">
            <c:out value="${file.name()}"/>
        </t:button>
    </c:forEach>
    <t:button onclick="uploadBin()" primary="true">New document</t:button>
</div>

</body>
</html>
