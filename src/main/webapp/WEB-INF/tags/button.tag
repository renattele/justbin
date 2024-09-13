<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ tag body-content="scriptless" %>
<%@ attribute name="style" %>
<%@ attribute name="onclick" %>
<%@ attribute name="primary" %>
<%@ attribute name="id" %>
<%@ attribute name="href" %>
<c:set var="cl" value="${(empty primary) ? 'hover-button' : 'hover-button-primary'}"/>

<c:if test="${not empty href}">
    <a href="${href}" style="${style}">
        <div id="${id}" onclick="${onclick}" class="hover-button-background" style="cursor: pointer">
            <div class="${cl}">
                <jsp:doBody/>
            </div>
        </div>
    </a>
</c:if>
<c:if test="${empty href}">
    <div id="${id}" onclick="${onclick}" class="hover-button-background" style="cursor: pointer; ${style}">
        <div class="${cl}">
            <jsp:doBody/>
        </div>
    </div>
</c:if>