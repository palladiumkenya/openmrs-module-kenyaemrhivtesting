<%
    ui.decorateWith("kenyaemr", "standardPage", [patient: currentPatient, layout: "sidebar"])
    def menuItems = [
            [label: "Back to home", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to home", href: ui.pageLink("hivtestingservices", "newEditPatientContactForm", [patientContact: patientContact.id])]
    ]
%>

<div class="ke-page-sidebar">
    <div class="ke-panel-frame">
        ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Navigation", items: menuItems])}
    </div>
</div>
<div class="ke-page-content">
    ${ ui.includeFragment("hivtestingservices", "contactTracingForm", [ patientContact: patientContact.id, returnUrl: ui.pageLink("hivtestingservices", "newEditPatientContactForm", [patientContact: patientContact.id]) ]) }
</div>