const markerContent = (data) => {
    const buildingInfo = Object.entries(data)
        .filter(f => f[0].startsWith("building_info_"))
        .map(([_, m]) => {
            const split = m.split(":");
            const count = split.shift();
            const name = split.join(":");
            return `<tr><td>${name}:</td><td>${count}</td></tr>`;
        })
        .sort((s1, s2) => $(s2).text() < $(s1).text() ? 1 : -1)
        .join("\n");
    const citizenInfo = Object.entries(data)
        .filter(f => f[0].startsWith("citizen_info_"))
        .map(([_, m]) => {
            const split = m.split(":");
            const name = atob(split[0]);
            return `<tr><td>${name}:</td><td>${split.length > 1 ? atob(split[1]) : "<i>Unemployed</i>"}</td></tr>`;
        })
        .sort((s1, s2) => $(s2).text() < $(s1).text() ? 1 : -1)
        .join("\n");
    return `
        <div class="minecolonies-marker">
            <div class="sizer"></div>
            <h2 class="${data.icon}">${data.colony}</h2>
            <div class="paragraph">
                <p>Mayor: ${data.mayor}</p>
                <p>Style: ${data.style}</p>
            </div>
        
            <hr/>
            
            <details class="expansion">
                <summary>
                    <span data-css-icon="down">Buildings: ${data.building_count}<i></i></span>
                </summary>
                <div class="content">
                    <table>
                        <tbody>
                            ${buildingInfo}
                      </tbody>
                    </table>
                </div>
            </details>
            
            <hr/>
            
            <details class="expansion">
                <summary>
                    <span data-css-icon="down">Population: ${data.citizen_count}<i></i></span>
                </summary>
                <div class="content">
                    <table>
                        <tbody>
                            ${citizenInfo}
                      </tbody>
                    </table>
                </div>
            </details>
        </div>
    `;
};

function setDetailsHeight(selector, wrapper = document) {
    const setHeight = (detail, open = false) => {
        detail.open = open;
        const rect = detail.getBoundingClientRect();
        detail.dataset.width = rect.width;
        detail.style.setProperty(open ? `--expanded` : `--collapsed`, `${rect.height}px`);
    }
    const details = wrapper.querySelectorAll(selector);
    const RO = new ResizeObserver(entries => {
        return entries.forEach(entry => {
            const detail = entry.target;
            const width = parseInt(detail.dataset.width, 10);
            if (width !== entry.contentRect.width) {
                detail.removeAttribute('style');
                setHeight(detail);
                setHeight(detail, true);
                detail.open = false;
            }
        })
    });
    details.forEach(detail => {
        RO.observe(detail);
    });
}

function updateMarkers() {
    setDetailsHeight(".minecolonies-marker .expansion");

    let shouldUpdate = false;
    for (const [key, value] of Object.entries(dynmapmarkersets["minecolonies-colony-markers"].markers)) {
        // Strip HTML entities away, caused by Dynmap it's sanitization.
        const parsed = $($.parseHTML(value.desc));
        const div = parsed.children("div").eq(0);
        if (div && div.html() === "") {
            const argsElement = parsed.children("img").eq(0);
            const args = argsElement.attr("alt").split(";").slice(1);

            const data = {};
            for (const arg of args) {
                const [argKey, argValue] = arg.split(":");
                data[argKey] = atob(argValue);
            }

            div.html(markerContent(data));
            dynmapmarkersets["minecolonies-colony-markers"].markers[key].desc = parsed.html();
            shouldUpdate = true;
        }
    }

    if (shouldUpdate) {
        $(dynmap).trigger("mapchanged");
    }
}

const waitForGlobal = function (key, callback) {
    if (window[key]) {
        callback(window[key]);
    } else {
        setTimeout(function () {
            waitForGlobal(key, callback);
        }, 100);
    }
};

waitForGlobal("dynmap", dynmap => {
    $(dynmap).on("markersupdated", updateMarkers);
    waitForGlobal("dynmapmarkersets", dynmap => {
        updateMarkers();
    });
});