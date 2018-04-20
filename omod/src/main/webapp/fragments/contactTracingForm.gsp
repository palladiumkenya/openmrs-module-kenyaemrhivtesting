<%
    ui.decorateWith("kenyaui", "panel", [heading: (command.original ? "Edit" : "Add") + " ContactTracing", frameOnly: true])

    def date = [
            [
                    [object: command, property: "date", label: "Date"]

            ]
    ]

    def patientContactTracing = [
            [
                    [object: command, property: "contactType", label: "Contact Type"],
                    [object: command, property: "status", label: "Status"]

            ],
    ]


    def linkageToCare = [
            [
                    [object: command, property: "facilityLinkedTo", label: "Facility Linked To"],
                    [object: command, property: "uniquePatientNo", label: "Unique Patient No."],
                    [object: command, property: "healthWorkerHandedTo", label: "Health Worker Linked To"]

            ]
    ]

    def remarks = [
            [
                    [object: command, property: "remarks", label: "Remarks"]
            ]
    ]


%>

<form id="patient-contact-trace-form" method="post"
      action="${ui.actionLink("hivtestingservices", "contactTracingForm", "saveClientTrace")}">


    <div class="ke-panel-content">

        <div class="ke-form-globalerrors" style="display: none"></div>

        <div class="ke-form-instructions">
            <strong>*</strong> indicates a required field
        </div>
    </div>
    <fieldset>
        <legend>Date</legend>

        <% date.each { %>
        ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
        <% } %>

    </fieldset>

    <fieldset>
        <legend>Contact Tracing</legend>
        <% patientContactTracing.each { %>
        ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
        <% } %>
    </fieldset>

    <fieldset>
        <legend>Linkage to Care</legend>
        <% linkageToCare.each { %>
        ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
        <% } %>
    </fieldset>

    <fieldset>
        <legend>Remarks</legend>
        <% remarks.each { %>
        ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
        <% } %>
    </fieldset>

    <div class="ke-panel-footer">
        <button type="submit">
            <img src="${ui.resourceLink("kenyaui", "images/glyphs/ok.png")}"/> ${command.original ? "Save Changes" : "Create Contact Trace"}
        </button>
        <% if (config.returnUrl) { %>
        <button type="button" class="cancel-button"><img
                src="${ui.resourceLink("kenyaui", "images/glyphs/cancel.png")}"/> Cancel</button>
        <% } %>
    </div>

</form>


<script type="text/javascript">

    //On ready
    jQuery(function () {
        //defaults

        jQuery('#patient-contact-trace-form .cancel-button').click(function () {
            ui.navigate('${ config.returnUrl }');
        });
        kenyaui.setupAjaxPost('patient-contact-trace-form', {
            onSuccess: function (data) {
                if (data.id) {
                    <% if (config.returnUrl) { %>
                    ui.navigate('${ config.returnUrl }');
                    <% } else { %>
                    ui.navigate('hivtestingservices', 'patientContactForm', {patientContactId: data.id});
                    <% } %>
                } else {
                    kenyaui.notifyError('Saving contact tracing was successful, but with unexpected response');
                }
            }
        });

    }); // end of jQuery initialization bloc


</script>