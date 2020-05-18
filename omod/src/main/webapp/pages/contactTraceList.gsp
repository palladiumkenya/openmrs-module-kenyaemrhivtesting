<%
    ui.decorateWith("kenyaemr", "standardPage", [patient: currentPatient, layout: "sidebar"])
    def menuItems = [
            [label: "Back to home", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to Contact List", href: ui.pageLink("hivtestingservices", "patientContactList", [patient: currentPatient, patientId: currentPatient.patientId])]
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
    width: 110px;
}

div.column-three {
    width: 120px;
}

div.column-four {
    width: 200px;
}

div.column-five {
    width: 160px;
}

div.column-six {
    width: 180px;
}

div.column-seven {
    width: 120px;
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
    <div class="ke-panel-frame">
        <div class="ke-panel-heading">Contact Tracing</div>

        <div class="ke-panel-content">
            ${ui.includeFragment("hivtestingservices", "patientContactProfile", [patientContact: patientContact.id])}
            <input type="hidden" name="lTraceStat" value="${lastTraceStatus}" id="lTraceStat"/>
            <fieldset>
                <legend>Trace History</legend>

                <div class="section-title"></div>

                <div class="clear"></div>

                <% if (traces) { %>
                <div class="grid">

                    <div class="column-one col-header">Date</div>

                    <div class="column-two col-header">Contact Type</div>

                    <div class="column-three col-header">Status</div>

                    <div class="column-four col-header">Reason not Contacted</div>

                    <div class="column-five col-header">Facility Linked To</div>

                    <div class="column-six col-header">Remarks</div>

                    <div class="column-seven col-header"></div>

                </div>

                <div class="clear"></div>

                <% traces.each { rel -> %>

                <div class="ke-stack-item ke-navigable">
                    <div class="grid">

                        <div class="column-one">${rel.date}</div>

                        <div class="column-two">${rel.contactType}</div>

                        <div class="column-three">${rel.status ?: ""}</div>

                        <div class="column-four">${rel.reasonUncontacted ?: ""}</div>

                        <div class="column-five">${rel.facilityLinkedTo ?: ""}</div>

                        <div class="column-six">${rel.remarks ?: ""}</div>

                        <% if (patientContact.patient == null) { %>

                        <div class="column-seven">
                            <button type="button"
                                    onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "newContactTraceForm", [ patientContact: patientContact.id, patientId: currentPatient.patientId, traceId: rel.id,  returnUrl: ui.thisUrl() ])}')">
                                <img src="${ui.resourceLink("hivtestingservices", "images/glyphs/edit.png")}"/> Edit
                            </button>
                        </div>
                        <% } %>

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

    <% if (patientContact.patient == null) { %>
    <div align="center">

        <button class="addTrace" name="addTrace" type="button"
                onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "newContactTraceForm", [ patientContact: patientContact.id, patientId: currentPatient.patientId,  returnUrl: ui.thisUrl() ])}')">
            <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/> Add Trace
        </button>

    <% if (patientContact.pnsApproach != null) { %>
            <button type="button"
                    onclick="ui.navigate('${ ui.pageLink("hivtestingservices", "registerContact", [ patientContact: patientContact.id, returnUrl: ui.pageLink("hivtestingservices", "patientContactList", [patient: currentPatient, patientId: currentPatient.patientId]) ])}')">
                <img src="${ui.resourceLink("kenyaui", "images/glyphs/patient_m.png")}"/> Register and Followup
            </button>
    <% } %>

</div>
    <% } %>

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

</script>
