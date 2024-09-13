<jsp:useBean id="collectionName" scope="request" type="java.lang.String"/>
<jsp:useBean id="collectionID" scope="request" type="java.lang.String"/>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<html lang="en">
<t:header title="Collection.">
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
            recent = recent.filter(c => c.id !== "${collectionID}");
            recent.push({
                id: "${collectionID}",
                name: document.getElementById("collection-name-input").value
            });
            localStorage.setItem("recent_collections", JSON.stringify(recent));
        }

        const changeName = debounce(1000, () => {
            onLoad()
            fetch("/c/${collectionID}/edit_name", {
                method: "POST",
                body: document.getElementById("collection-name-input").value
            })
        })

        function uploadBin() {

        }

        function createTxt() {

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
                console.log(formData);
                fetch("/c/${collectionID}/create_bin", {
                    method: "POST",
                    body: formData
                }).then(response => {
                    location.reload()
                })
            })
        }
    </script>
</t:header>
<body onload="onLoad(); loadDrag()" class="centered-container">
<input onfocusout="changeName()" id="collection-name-input" style="width: 100%; margin-top: 50px" size="20"
       value="${collectionName}" oninput="changeName()">
<div id="content-container" class="content-container" style="padding-top: 20px">
    <jsp:useBean id="files" scope="request" type="java.util.List"/>
    <c:forEach items="${files}" var="file">
        <a href="<c:url value="/v/${file.id()}"/>">
            <div class="hover-button-background">
                <div class="hover-button">
                    <div class="binary-file-card-name">${file.name()}</div>
                    <div class="binary-file-card-hint"></div>
                </div>
            </div>
        </a>
    </c:forEach>
    <div class="hover-button-background">
        <div class="hover-button-primary new-document-button">
            <div class="new-document-nohover">New document</div>
            <div style="display: flex; width: 100%">
                <div style="flex: 1; cursor: pointer" onclick="createTxt()" class="new-document-type">
                    txt
                </div>
                <div style="flex: 1; cursor:pointer;" onclick="uploadBin()" class="new-document-type">
                    bin
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>