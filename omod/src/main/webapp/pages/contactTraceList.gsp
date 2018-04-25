<%
    ui.decorateWith("kenyaemr", "standardPage", [patient: currentPatient, layout: "sidebar"])
    def menuItems = [
            [label: "Back to home", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to home", href: ui.pageLink("hivtestingservices", "patientContactList", [patient: currentPatient, patientId: currentPatient.patientId])]
    ]
%>
<style>
div.grid {
    display: block;
}
div.grid div {
    float: left;
    height: 30px;
}
div.column-one {
    width: 180px;
}
div.column-two {
    width: 140px;
}
div.column-three {
    width: 200px;
}
div.column-four {
    width: 200px;
}
div.column-five {
    width: 200px;
}
div.column-six {
    width: 200px;
}

div.clear {
    clear: both;
}
.col-header {
    font-weight: bold;
    font-size: 14px;
}
div.section-title {
    color: black;
    font-weight: bold;
    display: block;
    width: 550px;
    float: left;
    font-size: 16px;
}
</style>

<div class="ke-page-sidebar">
    <div class="ke-panel-frame">
        ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Navigation", items: menuItems])}
    </div>
</div>


<div class="ke-page-content">
    <div class="ke-panel-frame">
        <div class="ke-panel-heading">Contact Tracing</div>

        <div class="ke-panel-content">
            ${ui.includeFragment("hivtestingservices", "patientContactProfile", [patientContact: patientContact.id])}

            <fieldset>
                <legend>Trace History</legend>

                <div class="section-title"></div>

                <div class="clear"></div>

                <% if (traces) { %>
                <div class="grid">

                    <div class="column-one col-header">Date</div>

                    <div class="column-two col-header">Contact Type</div>

                    <div class="column-three col-header">Status</div>

                    <div class="column-four col-header">Facility Linked To</div>

                    <div class="column-five col-header">Health Worker Handed To</div>

                    <div class="column-six col-header">Remarks</div>
                    <div style="float: left; width: 60px">

                    </div>

                </div>

                <div class="clear"></div>

                <% traces.each { rel -> %>

                <div class="ke-stack-item">
                    <div class="grid">

                        <div class="column-one">${rel.date}</div>

                        <div class="column-two">${rel.contactType}</div>

                        <div class="column-three">${rel.status}</div>

                        <div class="column-four">${rel.facilityLinkedTo}</div>

                        <div class="column-five">${rel.healthWorkerHandedTo}</div>

                        <div class="column-six">${rel.remarks}</div>
                        <div style="float: left; width: 60px">
                            <button type="button"
                                    onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "newContactTraceForm", [ patientContact: patientContact.id, patientId: currentPatient.patientId, traceId: rel.id,  returnUrl: ui.thisUrl() ])}')">
                                <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/> Edit Trace
                            </button>
                        </div>

                    </div>

                    <div class="clear"></div>

                </div>
                <% }
                } else { %>
                No Contact trace found
                <% } %>
            </fieldset>
        </div>

        <div class="clear"></div>

    </div>

    <div align="center">

        <button type="button"
                onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "newContactTraceForm", [ patientContact: patientContact.id, patientId: currentPatient.patientId,  returnUrl: ui.thisUrl() ])}')">
            <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/> Add Trace
        </button>

    </div>
</div>
