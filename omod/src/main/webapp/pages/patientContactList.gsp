<%
    ui.decorateWith("kenyaemr", "standardPage", [patient: currentPatient, layout: "sidebar"])
    def menuItems = [
            [label: "Back to home", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to Client home", href: ui.pageLink("kenyaemr", "clinician/clinicianViewPatient", [patient: currentPatient, patientId: currentPatient.patientId])],
            [label: "View Tree", iconProvider: "hivtestingservices", icon: "buttons/contact_tree.png", label: "View Tree", href: ui.pageLink("hivtestingservices", "contactTreeView", [patient: currentPatient, patientId: currentPatient.patientId])]
    ]

    ui.includeCss("hivtestingservices", "TreantJs/Treant.css")
    ui.includeCss("hivtestingservices", "TreantJs/collapsable.css")
    ui.includeCss("hivtestingservices", "TreantJs/vendor/perfect-scrollbar/perfect-scrollbar.css")

    ui.includeJavascript("hivtestingservices", "TreantJs/vendor/raphael.js")
    ui.includeJavascript("hivtestingservices", "TreantJs/Treant.js")
    ui.includeJavascript("hivtestingservices", "TreantJs/jquery.easing.js")
    ui.includeJavascript("hivtestingservices", "TreantJs/collapsable.js")
    ui.includeJavascript("kenyaui", "kenyaui.js")

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
    width: 200px;
}

div.column-four {
    width: 120px;
}

div.column-five {
    width: 160px;
}

div.column-six {
    width: 180px;
}

div.column-seven {
    width: 140px;
}

div.column-eight {
    width: 100px;
}

div.column-eleven {
    width: 200px;
}

div.column-twelve {
    width: 180px;
}

div.column-thirteen {
    width: 220px;
}

div.column-fourteen {
    width: 200px;
}

div.column-fifteen {
    width: 240px;
}

div.column-sixteen {
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

                        <div class="column-three col-header">Sub County</div>

                         <div class="column-three col-header">Town</div>

                        <div class="column-four col-header">Phone</div>

                        <div class="column-five col-header">Relationship</div>

                        <div class="column-six col-header">Date of Last Contact</div>

                        <div class="column-seven col-header">Contact type</div>

                        <div class="column-eight col-header"></div>

                    </div>

                    <div class="clear"></div>

                    <% contacts.each { rel -> %>

                    <div class="ke-stack-item ke-navigable">
                        <div class="grid">

                            <div class="column-one">${rel.fullName}</div>

                            <div class="column-two">${rel.sex}</div>

                            <div class="column-three">${rel.subcounty ?: ""}</div>

                            <div class="column-three">${rel.town ?: ""}</div>

                            <div class="column-four">${rel.phoneContact ?: ""}</div>

                            <div class="column-five">${rel.relationType}</div>

                            <div class="column-seven">${rel.appointmentDate ?: ''}</div>

                            <div class="column-seven">${rel.pnsApproach ?: ''}</div>


                            <div class="column-eight">
                                <button type="button"
                                        onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "contactTraceList", [ patientContact: rel.id, patientId: currentPatient.patientId,  returnUrl: ui.thisUrl() ])}')">
                                    <img src="${ui.resourceLink("kenyaui", "images/glyphs/view.png")}"/>History
                                </button>
                            </div>

                            <div class="column-nine">

                                <button type="button"
                                        onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "newEditPatientContactForm", [ patientContactId: rel.id, patientId: currentPatient.id, returnUrl: ui.thisUrl() ])}')">
                                    <img src="${ui.resourceLink("kenyaui", "images/glyphs/edit.png")}"/> Edit
                                </button>

                            </div>

                            <% if (rel.patient == null) { %>
                            <div class="column-ten">
                                <button type="button"
                                        onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "registerContact", [ patientContact: rel.id, returnUrl: ui.thisUrl() ])}')">
                                    <img src="${ui.resourceLink("kenyaui", "images/glyphs/patient_m.png")}"/> Register
                                </button>
                        </div>
                            <div>
                                <button type="button" class="ke-compact"
                                        onclick="onVoidPatientContact(${ rel.id })">
                                    <img src="${ui.resourceLink("kenyaui", "images/glyphs/void.png")}"/> Delete
                                </button>
                            </div>

                            <% } else { %>
                            <div class="column-ten">
                                <button type="button"
                                        onclick="ui.navigate('${ ui.pageLink("kenyaemr", "hivTesting/htsViewPatient", [patientId: rel.patient.patientId])}')">
                                    <img src="${ui.resourceLink("kenyaui", "images/glyphs/patient_m.png")}"/> View
                                </button>
                            </div>
                            <% } %>
                            <div class="column-eleven">

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

                <button type="button" class="addContact"
                        onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "newEditPatientContactForm", [ patientId: patient.id,  returnUrl: ui.thisUrl() ])}')">
                    <img src="${ui.resourceLink("kenyaui", "images/glyphs/person_m.png")}"
                         style="display:none;"/>Add Contact
                </button>

            </div>

        </div>

        <div class="ke-tab" data-tabid="contact_trace">
            <div class="ke-panel-frame">
                <div class="ke-panel-heading">Tracing History of Patient</div>

                <div class="ke-panel-content">
                    <input type="hidden" name="lTraceStat" value="${lastTraceStatus}" id="lTraceStat"/>

                    <div class="section-title"></div>

                    <div class="clear"></div>

                    <% if (traces) { %>
                    <div class="grid">

                        <div class="column-eleven col-header">Date</div>

                        <div class="column-twelve col-header">Contact Type</div>

                        <div class="column-thirteen col-header">Status</div>

                        <div class="column-fourteen col-header">Facility Linked To</div>

                        <div class="column-fifteen col-header">Health Worker Handed To</div>

                        <div class="column-sixteen col-header">Remarks</div>

                        <div style="float: left; width: 60px">

                        </div>

                    </div>

                    <div class="clear"></div>

                    <% traces.each { rel -> %>

                    <div class="ke-stack-item">
                        <div class="grid">

                            <div class="column-eleven">${rel.date}</div>

                            <div class="column-twelve">${rel.contactType}</div>

                            <div class="column-thirteen">${rel.status}</div>

                            <div class="column-fourteen">${rel.facilityLinkedTo}</div>

                            <div class="column-fifteen">${rel.healthWorkerHandedTo ?: ""}</div>

                            <div class="column-sixteen">${rel.remarks}</div>

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

                <button class="addTrace" name="addTrace" type="button"
                        onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "newContactTraceForm", [ patientContact: patientContact.id, patientId: currentPatient.patientId,  returnUrl: ui.thisUrl() ])}')">
                    <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/> Add Trace
                </button>

            </div>
            <% } %>

        </div>

    </div>

</div>

<script type="text/javascript">

    //On ready
    jQuery(function () {

        var result;
        var lastTrace;


        lastTrace = jq('#lTraceStat').val();
        result = jq('#lTraceStat').val().localeCompare("Contacted and Linked");

        if (result == 0) {
            jq(".addTrace").hide();
        }
        else {
            jq(".addTrace").show();
        }

    }); // end of jQuery initialization bloc

    function openContactsTree() {
        kenyaui.openPanelDialog({templateId: 'contact-tree', width: 85, height: 70, scrolling: true});
    }

    function onVoidPatientContact(contactId) {
        kenyaui.openConfirmDialog({
            heading: 'Void Patient Contact',
            message: '${ ui.message("Are you sure you want to void this contact?") }',
            okCallback: function () {
                voidPatientContact(contactId);
            }
        });
    }

    function voidPatientContact(contactId) {
        ui.getFragmentActionAsJson('hivtestingservices', 'htsUtils', 'voidContact', {id: contactId}, function () {
            ui.reloadPage();
        });
    }

</script>