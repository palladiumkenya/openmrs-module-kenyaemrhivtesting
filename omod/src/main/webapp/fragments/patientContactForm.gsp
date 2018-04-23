<%
    ui.decorateWith("kenyaui", "panel", [heading: (command.original ? "Edit" : "Add") + " Contacts", frameOnly: true])

    def nameFields = [
            [
                    [object: command, property: "firstName", label: "First Name"],
                    [object: command, property: "middleName", label: "Middle Name"],
                    [object: command, property: "lastName", label: "Last Name"]
            ],
    ]


    def addressRows = [
            [
                    [object: command, property: "physicalAddress", label: "Physical Address"],
                    [object: command, property: "phoneContact", label: "Phone No."]

            ]
    ]

    def relTypeOptions = [
            [
                    [object: command, property: "relationType", label: "Relationship to patient", config: [options: relationshipTypeOptions]]
            ]
    ]


    def hivData = [
            [


                    [object: command, property: "baselineHivStatus", label: "Baseline HIV status"],
                    [object: command, property: "appointmentDate", label: "Appointment date"],
                    [object: command, property: "ipvOutcome", label: "IPV Outcome"]

            ]
    ]

%>

<form id="edit-patient-contact-form" method="post"
      action="${ui.actionLink("hivtestingservices", "patientContactForm", "savePatientContact")}">
<% if (command.original) { %>
<input type="hidden" name="id" value="${command.original.id}"/>
<% } %>

<div class="ke-panel-content">

    <div class="ke-form-globalerrors" style="display: none"></div>

    <div class="ke-form-instructions">
        <strong>*</strong> indicates a required field
    </div>

    <fieldset>
        <legend>Demographics</legend>
        <input type="hidden" name="patientRelatedTo" value="${currentPatient.id}"/>
        <% nameFields.each { %>
        ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
        <% } %>

        <table>
            <tr>
                <td valign="top">
                    <label class="ke-field-label">Sex *</label>
                    <span class="ke-field-content">
                        <input type="radio" name="sex" value="F"
                               id="gender-F" ${command.sex == 'F' ? 'checked="checked"' : ''}/> Female
                        <input type="radio" name="sex" value="M"
                               id="gender-M" ${command.sex == 'M' ? 'checked="checked"' : ''}/> Male
                        <span id="gender-F-error" class="error" style="display: none"></span>
                        <span id="gender-M-error" class="error" style="display: none"></span>
                    </span>
                </td>
                <td valign="top"></td>
                <td valign="top">
                    <label class="ke-field-label">Date of Birth *</label>
                    <span class="ke-field-content">
                        ${ui.includeFragment("kenyaui", "widget/field", [id: "patient-birthdate", object: command, property: "birthDate"])}

                        <span id="from-age-button-placeholder"></span>
                    </span>
                </td>
            </tr>
        </table>

    </fieldset>

</div>

<fieldset>
    <legend>Contact</legend>

    <% addressRows.each { %>
    ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
    <% } %>

</fieldset>

<fieldset>
    <legend>Relationship</legend>
    <% relTypeOptions.each { %>
    ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
    <% } %>
</fieldset>

<fieldset class="ipvAssessmentTable">
    <legend>IPV Assessment</legend>
    <table>
        <tr>
            <td>
                <label class="ke-field-label">1. Has he/she ever hit, kicked, slapped, or otherwise physically hurt you?</label>
            </td><td>
            <span class="ke-field-content">
                <input type="radio" name="question-1" class="question-1" value="1065"/> Yes
                <input type="radio" name="question-1" class="question-1" value="1066"/> No
            </span>
        </td>
        </tr>
        <tr>
            <td>
                <label class="ke-field-label">2. Has he/she ever threatened to hurt you?</label>
            </td><td>
            <span class="ke-field-content">
                <input type="radio" name="question-2" class="question-2" value="1065"/> Yes
                <input type="radio" name="question-2" class="question-2" value="1066"/> No
            </span>
        </td>
        </tr>
        <tr>
            <td>
                <label class="ke-field-label">3.Has he/she ever forced you to do something sexually that made you feel uncomfortable?</label>
            </td><td>
            <span class="ke-field-content">
                <input type="radio" name="question-3" class="question-3" value="1065"/> Yes
                <input type="radio" name="question-3" class="question-3" value="1066"/> No
            </span>
        </td>
        </tr>
    </table>
</fieldset>

<fieldset>
    <legend>hivData</legend>
    <% hivData.each { %>
    ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
    <% } %>
</fieldset>

</fieldset>

<div class="ke-panel-footer">
    <button type="submit">
        <img src="${ui.resourceLink("kenyaui", "images/glyphs/ok.png")}"/> ${command.original ? "Save Changes" : "Create Patient Contact"}
    </button>
    <% if (config.returnUrl) { %>
    <button type="button" class="cancel-button"><img
            src="${ui.resourceLink("kenyaui", "images/glyphs/cancel.png")}"/> Cancel</button>
    <% } %>
</div>

</form>

<!-- You can't nest forms in HTML, so keep the dialog box form down here -->
${ui.includeFragment("kenyaui", "widget/dialogForm", [
        buttonConfig     : [id: "from-age-button", label: "from age", iconProvider: "kenyaui", icon: "glyphs/calculate.png"],
        dialogConfig     : [heading: "Calculate Birthdate", width: 40, height: 40],
        fields           : [
                [label: "Age in years", formFieldName: "age", class: java.lang.Integer],
                [
                        label: "On date", formFieldName: "now",
                        class: java.util.Date, initialValue: new java.text.SimpleDateFormat("yyyy-MM-dd").parse((new Date().getYear() + 1900) + "-06-15")
                ]
        ],
        fragmentProvider : "kenyaemr",
        fragment         : "emrUtils",
        action           : "birthdateFromAge",
        onSuccessCallback: "updateBirthdate(data);",
        onOpenCallback   : """jQuery('input[name="age"]').focus()""",
        submitLabel      : ui.message("general.submit"),
        cancelLabel      : ui.message("general.cancel")
])}

<script type="text/javascript">

    //On ready
    jQuery(function () {
        //defaults

        jQuery('#from-age-button').appendTo(jQuery('#from-age-button-placeholder'));
        jQuery('#edit-patient-contact-form .cancel-button').click(function () {
            ui.navigate('${ config.returnUrl }');
        });
        kenyaui.setupAjaxPost('edit-patient-contact-form', {
            onSuccess: function (data) {
                if (data.id) {
                    <% if (config.returnUrl) { %>
                    ui.navigate('${ config.returnUrl }');
                    <% } else { %>
                    ui.navigate('hivtestingservices', 'patientContactList', {patientId: data.id});
                    <% } %>
                } else {
                    kenyaui.notifyError('Saving patient contact was successful, but with unexpected response');
                }
            }
        });

        //IPV validation
        jq(".ipvAssessmentTable").hide();
        jq("select[name='relationType']").change(function () {

            var relType = jq(this).val();

            console.log('Relationship type ' + relType);

        /*    if (relType === "Spouse" || relType === "Partner") */
             if (relType === "6" || relType === "7") {

                jq('.ipvAssessmentTable').show();
            }
            else {
                jq('.ipvAssessmentTable').hide();
            }
        });

    }); // end of jQuery initialization block

    function updateBirthdate(data) {
        var birthdate = new Date(data.birthdate);
        kenyaui.setDateField('patient-birthdate', birthdate);

    }


</script>