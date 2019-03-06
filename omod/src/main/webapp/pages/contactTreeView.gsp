<%
    ui.decorateWith("kenyaemr", "standardPage", [patient: currentPatient, layout: "sidebar"])
    def menuItems = [
            [label: "Back to Contacts", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to Contact List", href: ui.pageLink("hivtestingservices", "patientContactList", [patientId: currentPatient.patientId])]
    ]


%>
<div class="ke-page-sidebar">
    <div class="ke-panel-frame">
        ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Navigation", items: menuItems])}
    </div>
</div>
<div class="ke-page-content">

    ${ ui.includeFragment("hivtestingservices", "contactTreeView", [patientId: currentPatient.patientId]) }

</div>