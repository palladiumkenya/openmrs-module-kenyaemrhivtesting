<%
    ui.decorateWith("kenyaemr", "standardPage", [patient: currentPatient, layout: "sidebar"])
    def menuItems = [
            [label: "Back to home", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to home", href: ui.pageLink("kenyaemr", "clinician/clinicianViewPatient", [patient: currentPatient, patientId: currentPatient.patientId])]
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
    width: 80px;
}

div.column-three {
    width: 160px;
}

div.column-four {
    width: 120px;
}

div.column-five {
    width: 120px;
}

div.column-six {
    width: 140px;
}

div.column-seven {
    width: 140px;
}

div.column-eight {
    width: 120px;
}

div.column-nine {
    width: 100px;
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
        <div class="ke-panel-heading">Patient Contacts</div>

        <div class="ke-panel-content">
            <div class="section-title"></div>

            <div class="clear"></div>

            <% if (contacts) { %>
            <div class="grid">

                <div class="column-one col-header">Name</div>

                <div class="column-two col-header">Gender</div>

                <div class="column-three col-header">Physical Address</div>

                <div class="column-four col-header">Phone</div>

                <div class="column-five col-header">Relationship</div>

                <div class="column-six col-header">Baseline HIV Test</div>

                <div class="column-seven col-header">Appointment Date</div>

                <div class="column-eight col-header"></div>

                <div class="column-nine col-header"></div>
            </div>

            <div class="clear"></div>

            <% contacts.each { rel -> %>

            <div class="ke-stack-item ke-navigable" ng-click="onResultClick(patientContact)">
                <div class="grid">

                    <div class="column-one">${rel.lastName + ' ' + rel.firstName + ' ' + rel.middleName}</div>

                    <div class="column-two">${rel.sex}</div>

                    <div class="column-three">${rel.physicalAddress}</div>

                    <div class="column-four">${rel.phoneContact}</div>

                    <div class="column-five">${rel.relationType}</div>

                    <div class="column-six">${rel.baselineHivStatus}</div>

                    <div class="column-seven">${rel.appointmentDate}</div>

                    <div class="column-eight">


                        <button type="button"
                                onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "contactTraceList", [ patientContact: rel.id,patientId: currentPatient.id, returnUrl: ui.thisUrl() ])}')">
                            <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/> History
                        </button>

                    </div>

                    <div class="column-nine">
                        <button type="button"
                                onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "newEditPatientContactForm", [ patientContact: rel.id,patientId: currentPatient.id, returnUrl: ui.thisUrl() ])}')">
                            <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/> Edit
                        </button>
                    </div>

                </div>

                <div class="clear"></div>

            </div>
            <% }
            } else { %>
            No Patient Contact found
            <% } %>
        </div>

        <div class="clear"></div>

    </div>

    <div align="center">

        <button type="button"
                onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "newEditPatientContactForm", [ patientId: patient.id,  returnUrl: ui.thisUrl() ])}')">
            <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/>Add Contact
        </button>

    </div>

</div>

