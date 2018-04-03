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
    width: 300px;
}

div.column-two {
    width: 100px;
}

div.column-three {
    width: 120px;
}

div.column-four {
    width: 120px;
}

div.column-five {
    width: 120px;
}

div.column-six {
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
            <div class="section-title">Patient Contacts list</div>

            <div class="clear"></div>
            <% if (contacts) { %>
            <div class="grid">
                <div class="column-one">&nbsp;</div>

                <div class="column-one">First Name</div>

                <div class="column-two col-header">Middle name</div>

                <div class="column-three col-header">Last Name</div>

                <div class="column-four col-header">Sex</div>

                <div class="column-five col-header">Physical Address</div>

                <div class="column-six col-header">Phone</div>
            </div>

            <div class="clear"></div>

            <% contacts.each { rel -> %>

            <div class="ke-stack-item">
                <div class="grid">
                    <div class="column-one">&nbsp;</div>

                    <div class="column-two">${rel.firsName}</div>

                    <div class="column-three">${rel.middleName}</div>

                    <div class="column-four">${rel.lastName}</div>

                    <div class="column-five">${rel.sex}</div>

                    <div class="column-six">${rel.physicalAddress}</div>

                    <div class="column-seven">${rel.phone}</div>
                </div>

                <div class="clear"></div>
            </div>

        </div>

        <div class="clear"></div>
    </div>
    <% } } else { %>
    No Patient Contact found
    <% } %>
</div>



