<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<t:header title="JustBin.">
    <script>
        function createCollection() {
            fetch("/c/create", {
                method: "POST"
            }).then(response => {
                console.log(response)
                if (response.status !== 200) {
                    return null;
                }
                return response.text()
            })
                .then(response => {
                        if (typeof response == 'string') {
                            location.href = "/c/" + response
                        }
                    }
                )
        }

        function updateCollections() {
            let recent = getRecent();
            let html = "";
            if (recent.length !== 0) {
                html += `
                <div onclick="clearRecent()" style="cursor: pointer">
                    <p>clear</p>
                </div>
                `
            }
            recent.forEach(collection => {
                html += `
            <t:button href="$href" style="width: 280px">$collection</t:button>
            `.replace("$collection", collection.name).replace("$href", "/c/" + collection.id)
            })
            console.log(html)
            document.getElementById("recents").innerHTML = html
        }

        function clearRecent() {
            localStorage.removeItem("recent_collections")
            updateCollections()
        }
    </script>
</t:header>
<body onpageshow="updateCollections()" class="centered-container-scrollable" style="gap: 20px">
<h1>JustBin.</h1>
<t:button onclick="createCollection()" style="width: 280px">New collection</t:button>
<t:button href="themes" style="width: 280px; margin-bottom: 100px">Themes</t:button>
<div id="recents" style="display: inline-flex; flex-direction: column; gap: 20px">

</div>
</body>
</html>
