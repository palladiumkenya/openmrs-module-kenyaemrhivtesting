<%
    ui.decorateWith("kenyaui", "panel", [heading: (command.original ? "Edit" : "Add") + " Patient Contact", frameOnly: true])

    def nameFields = [
            [
                    [object: command, property: "firstName", label: "First Name"],
                    [object: command, property: "middleName", label: "Middle Name"],
                    [object: command, property: "lastName", label: "Last Name"]

            ]
    ]


    def addressRows = [
            [
                    [object: command, property: "physicalAddress", label: "Address"],
                    [object: command, property: "subcounty", label: "Sub county"],
                    [object: command, property: "town", label: "Town"],
                    [object: command, property: "phoneContact", label: "Phone number."]

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
                    <td valign="top"></td>
                    <td valign="top">
                </tr>
            </table>

        </fieldset>

        <fieldset>
            <legend>Address</legend>

            <% addressRows.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>

        </fieldset>

        <fieldset>
            <legend>Relationship</legend>
            <table>
                <tr>
                    <td class="ke-field-label">Relation to case</td>
                    <td class="ke-field-label">Date of Last Contact</td>
                    <td class="ke-field-label">Head of household</td>
                    <td class="ke-field-label">Contact is a Healthcare Worker</td>
                    <td></td>
                    <td class="ke-field-label">Facility</td>
                </tr>
                <tr>
                    <td style="width: 260px">
                        <select name="relationType" id="relationType">
                            <option></option>
                            <% relationshipTypeOptions.each { %>
                            <option ${
                                    (command.relationType == null) ? "" : it.value == command.relationType ? "selected" : ""}
                                    value="${it.value}">${it.label}</option>
                            <% } %>
                        </select>
                    </td>
                    <td style="width: 200px">
                        ${ui.includeFragment("kenyaui", "widget/field", [object: command, property: "appointmentDate"])}
                    </td>
                    <td style="width: 260px">
                        <select name="livingWithPatient" id="livingWithPatient">
                            <option></option>
                            <% livingWithPatientOptions.each { %>
                            <option ${
                                    (command.livingWithPatient == null) ? "" : it.value == command.livingWithPatient ? "selected" : ""}
                                    value="${it.value}">${it.label}</option>
                            <% } %>
                        </select>
                    </td>
                    <td>
                        <select name="maritalStatus" id="maritalStatus">
                            <option></option>
                            <% maritalStatusOptions.each { %>
                            <option ${
                                    (command.maritalStatus == null) ? "" : it.value == command.maritalStatus ? "selected" : ""}
                                    value="${it.value}">${it.label}</option>
                            <% } %>
                        </select>
                    </td>
                    <td></td>
                    <td>
                        <textarea class ="facility" name="facility"  rows="0" cols="15">${(command.facility != null)? command.facility : ""}</textarea>
                    </td>
                </tr>
            </table>
        </fieldset>


        <fieldset>
            <legend>Contact type</legend>
            <table>
                <tr>
                    <td class="ke-field-label">Type of contact</td>
                </tr>
                <tr>
                    <td style="width: 260px">
                        <select name="pnsApproach" id="pnsApproach">
                            <option></option>
                            <% preferredPNSApproachOptions.each { %>
                            <option ${
                                    (command.pnsApproach == null) ? "" : it.value == command.pnsApproach ? "selected" : ""}
                                    value="${it.value}">${it.label}</option>
                            <% } %>
                        </select>
                    </td>
                </tr>
            </table>
        </fieldset>


        <div class="ke-panel-footer">
            <button type="submit">
                <img src="${ui.resourceLink("kenyaui", "images/glyphs/ok.png")}"/> ${command.original ? "Save Changes" : "Save Patient Contact"}
            </button>
            <% if (config.returnUrl) { %>
            <button type="button" class="cancel-button"><img
                    src="${ui.resourceLink("kenyaui", "images/glyphs/cancel.png")}"/> Cancel</button>
            <% } %>

        </div>

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

        //HCW Exposure validation
        jq('.facility').prop('disabled', true);
        jq('.facility').prop('disabled', true);
        jq("select[name='maritalStatus']").change(function () {

            var exposureType = jq(this).val();

            console.log('HCW exposure type ' + exposureType);

            if (exposureType === "1065"/* Spouse or Partner*/) {

                jq('.facility').prop('disabled', false);
                jq('.facility').prop('disabled', false);
            }
            else {
                jq('.facility').prop('disabled', true);
                jq('.facility').prop('disabled', true);
            }
        });

    }); // end of jQuery initialization block

    function updateBirthdate(data) {
        var birthdate = new Date(data.birthdate);
        kenyaui.setDateField('patient-birthdate', birthdate);

    }

</script>
