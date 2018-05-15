<%
    ui.decorateWith("kenyaemr", "standardPage", [patient: currentPatient, layout: "sidebar"])
    def menuItems = [
            [label: "Back to home", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to Client home", href: ui.pageLink("kenyaemr", "clinician/clinicianViewPatient", [patient: currentPatient, patientId: currentPatient.patientId])]
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
    width: 100px;
}

div.column-nine {
    width: 80px;
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

    <div id="program-tabs" class="ke-tabs">
        <div class="ke-tabmenu">
            <div class="ke-tabmenu-item" data-tabid="contact_list">List of Contacts</div>

            <div class="ke-tabmenu-item" data-tabid="contact_trace">Client Tracing History</div>

        </div>
        <div class="ke-tab" data-tabid="contact_list">
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

                    <div class="ke-stack-item ke-navigable">
                        <div class="grid">

                            <div class="column-one">${rel.fullName}</div>

                            <div class="column-two">${rel.sex}</div>

                            <div class="column-three">${rel.physicalAddress?:""}</div>

                            <div class="column-four">${rel.phoneContact?:""}</div>

                            <div class="column-five">${rel.relationType}</div>

                            <div class="column-six">${rel.baselineHivStatus?:''}</div>

                            <div class="column-seven">${rel.appointmentDate?:''}</div>

                            <div class="column-eight">


                                <button type="button"
                                        onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "contactTraceList", [ patientContact: rel.id, patientId: currentPatient.patientId,  returnUrl: ui.thisUrl() ])}')">
                                    <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/>History
                                </button>


                            </div>

                            <div class="column-nine">
                                <button type="button"
                                        onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "newEditPatientContactForm", [ patientContactId: rel.id, patientId: currentPatient.id, returnUrl: ui.thisUrl() ])}')">
                                    <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/> Edit
                                </button>
                            </div>

                            <div style="float: left;width: 100px">
                                <button type="button"
                                        onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "registerContact", [ patientContact: rel.id, returnUrl: ui.thisUrl() ])}')">
                                    <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/> Register
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
        <div class="ke-tab" data-tabid="contact_trace">
            <div class="ke-panel-frame">
                <div class="ke-panel-heading">Tracing History of Patient</div>

                <div class="ke-panel-content">
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

                </div>

                <div class="clear"></div>

            </div>
            <% }
            } else { %>
            No Contact trace found
            <% } %>
                </div>
            </div>
            <div class="clear"></div>
            <% if (patientContact) { %>
            <div align="center">

                <button type="button"
                        onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "newContactTraceForm", [ patientContact: patientContact.id, patientId: currentPatient.patientId,  returnUrl: ui.thisUrl() ])}')">
                    <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/> Add Trace
                </button>

            </div>
            <% }%>

        </div>

    </div>



</div>
