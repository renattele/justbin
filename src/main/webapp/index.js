function loadTheme(css = localStorage.getItem("theme")) {
    document.getElementById("user-style").textContent = css;
}

function setTheme(string) {
    localStorage.setItem("theme", string);
    loadTheme()
}

function getRecent() {
    let recent = JSON.parse(localStorage.getItem("recent_collections"));
    if (recent == null) recent = Array.of();
    recent.reverse();
    return recent;
}

function cssFrom(background, foreground, css) {
    return rootCssFrom(background, foreground, css);
}

function rootCssFrom(background, foreground, css) {
    return `
    ${css}
    :root {
        --background-color: ${background};
        --foreground-color: ${foreground};
    }
    `;
}

function base64ToBytes(base64) {
    const binString = atob(base64);
    // noinspection JSCheckFunctionSignatures
    return Uint8Array.from(binString, (m) => m.codePointAt(0));
}

function bytesToBase64(bytes) {
    // noinspection JSCheckFunctionSignatures
    const binString = Array.from(bytes, (byte) =>
        String.fromCodePoint(byte),
    ).join("");
    return btoa(binString);
}

function base64(input) {
    return bytesToBase64(new TextEncoder().encode(input))
}

function unbase64(input) {
    return new TextDecoder().decode(base64ToBytes(input))
}

function navigateBack() {
    if (history.length === 1) {
        location.href = "/";
    } else {
        history.back();
    }
}

function reloadOnBack() {
    window.addEventListener('pageshow', function (event) {
        if (event.persisted || performance.getEntriesByType("navigation")[0].type === 'back_forward') {
            reload()
        }
    });
}

function reload() {
    window.location = window.location
}

function debounce(timeoutMs, callee) {
    return (...args) => {
        let previousCall = this.lastCall
        this.lastCall = Date.now()
        if (previousCall && this.lastCall - previousCall <= timeoutMs) {
            clearTimeout(this.lastCallTimer)
        }
        this.lastCallTimer = setTimeout(() => callee(...args), timeoutMs)
    }
}

loadTheme()
window.addEventListener("pageshow", () => loadTheme())

Number.prototype.clamp = function(min, max) {
    return Math.min(Math.max(this, min), max);
};
