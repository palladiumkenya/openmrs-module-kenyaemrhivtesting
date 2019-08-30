<%
    ui.decorateWith("kenyaui", "panel", [heading: (command.original ? "Edit" : "Add") + " Contact Tracing", frameOnly: true])

    def date = [
            [
                    [object: command, property: "date", label: "Date"]

            ]
    ]

    def patientContactTracing = [
            [

                    [object: command, property: "contactType", label: "Contact Type"],
                    [object: command, property: "status", label: "Status"],
                    [object: command, property: "reasonUncontacted", label: "Reason not Contacted"]

            ]
    ]


    def linkageToCare = [
            [
                    [object: command, property: "facilityLinkedTo", label: "Facility Linked To"],
                    [object: command, property: "healthWorkerHandedTo", label: "Health Worker Handed To"],
                    [object: command, property: "uniquePatientNo", label: "Unique Patient No."]

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
    <input type="hidden" name="patientContact" value="${patientContact.id}"/>
    <% if (command.original) { %>
    <input type="hidden" name="id" value="${command.original.id}"/>
    <% } %>

    <div class="ke-panel-content">

    ${ui.includeFragment("hivtestingservices", "patientContactProfile", [patientContact: patientContact.id])}
        <div class="ke-form-globalerrors" style="display: none"></div>

        <div class="ke-form-instructions">
            <strong>*</strong> indicates a required field
        </div>

        <fieldset>
            <legend>Tracing</legend>

            <% date.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>
            <table>
                <tr>
                    <td class="ke-field-label">Contact Type</td>
                    <td class="ke-field-label">Outcome</td>
                    <td id="reasons-ke" class="ke-field-label ">Reason not Contacted</td>
                </tr>
                <tr>
                    <td style="width: 270px">
                        <select name="contactType" id="contactType">
                            <option></option>
                            <% contactOptions.each { %>
                            <option ${(command.contactType == null)? "" : it == command.contactType ? "selected" : ""} value="${it}">${it}</option>
                            <% } %>
                        </select>
                    </td>
                    <td style="width: 260px">
                        <select name="status" id="tracingOutcome">
                            <option></option>
                            <% tracingOutcomeOptions.each { %>
                            <option ${(command.status == null)? "" : it == command.status ? "selected" : ""} value="${it}">${it}</option>
                            <% } %>
                        </select>
                    </td>
                    <td style="width: 260px">
                        <div id="phoneSection">
                        <select name="reasonUncontacted" id="uncontactedReasonPhone">
                            <option></option>
                            <% reasonUncontactedByPhone.each { %>
                            <option ${(command.reasonUncontacted == null)? "" : it == command.reasonUncontacted ? "selected" : ""} value="${it}">${it}</option>
                            <% } %>
                        </select>
                        </div>
                        <div id="physicalSection">
                            <select name="reasonUncontacted" id="uncontactedReasonPhysical">
                                <option></option>
                                <% reasonUncontactedByPhysical.each { %>
                                <option ${(command.reasonUncontacted == null)? "" : it == command.reasonUncontacted ? "selected" : ""} value="${it}">${it}</option>
                                <% } %>
                            </select>
                        </div>
                    </td>

                </tr>
            </table>
        </fieldset>
     <fieldset id="linkageSection">
            <legend>Linkage Details</legend>
            <% linkageToCare.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>
        </fieldset>

        <fieldset>
            <legend>Remarks</legend>
            <table>
                <tr>
                    <td class="ke-field-label">Provider Remarks</td>
                </tr>
                <tr>
                    <td>
                        <textarea name="remarks" rows="5" cols="80">${(command.remarks != null)? command.remarks : ""}</textarea>
                    </td>
                </tr>
            </table>
        </fieldset>

        <div class="ke-panel-footer">
            <button type="submit">
                <img src="${ui.resourceLink("kenyaui", "images/glyphs/ok.png")}"/> ${command.original ? "Save Changes" : "Create Contact Trace"}
            </button>

            <button type="button" class="cancel-button"><img
                    src="${ui.resourceLink("kenyaui", "images/glyphs/cancel.png")}"/> Cancel</button>

        </div>
    </div>
</form>


<script type="text/javascript">

    //On ready
    jQuery(function () {
        //defaults
        jQuery("#reasons-ke").hide(); //hide header
       // jQuery("#phoneSection select").prop("disabled", true); //disable select section
        jQuery("#physicalSection").hide(); //hide physical section
        jQuery("#phoneSection").hide(); //hide phone section

        <% if (command.original != null  &&  command.status == "Contacted and Linked") { %>
            jQuery("#linkageSection").show(); //hide linkage section
            <% } else { %>
            jQuery("#linkageSection").hide(); //hide linkage section
        <% } %>

        jQuery('#patient-contact-trace-form .cancel-button').click(function () {
            ui.navigate('${ config.returnUrl }');
        });
        kenyaui.setupAjaxPost('patient-contact-trace-form', {
            onSuccess: function (data) {
                if (data.patientContactId) {
                    <% if (config.returnUrl) { %>
                    ui.navigate('${ config.returnUrl }');
                    <% } else { %>
                    ui.navigate('hivtestingservices', 'contactTraceList', {patientId: patient.patientId, patientContact:patientContact.id});
                    <% } %>
                } else {
                    kenyaui.notifyError('Saving contact tracing was successful, but with unexpected response');
                }
            }
        });

        jQuery('#tracingOutcome').change(function () {
            var selectedOutcome = jQuery(this).val();
            var contactType = jQuery("#contactType").val();

            if(contactType != "") {
                if (selectedOutcome == "Contacted and Linked") {
                    jQuery("#linkageSection").show();
                } else {
                    jQuery("#linkageSection input").val("");
                    jQuery("#linkageSection").hide();
                }
            }
        });


        jQuery('#contactType').change(function () {
            var contactType = jQuery(this).val();
            var outcome = jQuery("#tracingOutcome").val();

            if(contactType != "" && outcome != "") {
                if (outcome == "Contacted and Linked") {
                    jQuery("#linkageSection").show();
                } else {
                    jQuery("#linkageSection input").val("");
                    jQuery("#linkageSection").hide();
                }
            }
        });

        jQuery('#contactType').change(function () {
            var contactType = jQuery(this).val();
            var outcome = jQuery("#tracingOutcome").val();

            if(contactType != "" && outcome != "") {
                if (contactType == "Phone" && outcome == "Not Contacted") {

                    jQuery("#phoneSection").show(); //hide phone section
                    jQuery("#phoneSection select").prop("disabled", false); //disable select section
                    jQuery("#physicalSection").hide(); //hide phone section
                    jQuery("#physicalSection select").val("");
                }
                if (contactType == "Physical" && outcome == "Not Contacted") {

                    jQuery("#physicalSection").show(); //hide phone section{
                    jQuery("#physicalSection select").prop("disabled", false); //disable select section
                    jQuery("#phoneSection").hide(); //hide phone section
                    jQuery("#phoneSection select").val("");
                }
            }
        });

        jQuery('#tracingOutcome').change(function () {
            var outcome = jQuery(this).val();
            var contactType = jQuery("#contactType").val();

            if(contactType != "" && outcome != "") {

                if(outcome == "Not Contacted") {

                    if (contactType == "Phone") {
                        jQuery("#reasons-ke").show(); //show header
                        jQuery("#phoneSection").show(); //hide phone section
                        jQuery("#phoneSection select").prop("disabled", false); //disable select section
                        jQuery("#physicalSection").hide(); //hide phone section
                        jQuery("#physicalSection select").val("");

                    } else if (contactType == "Physical") {
                        jQuery("#reasons-ke").show(); //show header
                        jQuery("#physicalSection").show(); //hide phone section{
                        jQuery("#physicalSection select").prop("disabled", false); //disable select section
                        jQuery("#phoneSection").hide(); //hide phone section
                        jQuery("#phoneSection select").val("");
                    }
                }else {
                        jQuery("#reasons-ke").hide(); //hide header
                        jQuery("#physicalSection").hide(); //hide phone section{
                        jQuery("#physicalSection select").val("");
                        jQuery("#phoneSection").hide(); //hide phone section
                        jQuery("#phoneSection select").val("");
                }
            }
        });

    }); // end of jQuery initialization bloc


</script>