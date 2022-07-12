<%
    ui.decorateWith("kenyaui", "panel", [heading: (command.original ? "Edit" : "Add") + " Patient Contact", frameOnly: true])

    def listingDate = [
            [
                    [object: command, property:"listingDate", label: "Date"]
            ],
    ]
    def nameFields = [
            [
                    [object: command, property: "firstName", label: "First Name"],
                    [object: command, property: "middleName", label: "Middle Name"],
                    [object: command, property: "lastName", label: "Last Name"]

            ],
    ]


    def addressRows = [
            [
                    [object: command, property: "physicalAddress", label: "Physical Address/Landmark"],
                    [object: command, property: "phoneContact", label: "Phone No."]

            ]
    ]

    def hivData = [
            [


                    [object: command, property: "baselineHivStatus", label: "Baseline HIV status"],
                    [object: command, property: "dateTested", label: "Date tested"],
                    [object: command, property: "appointmentDate", label: "Booking date"]


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
        <table>
            <tr>
                <td class="ke-field-label">Listing Date*</td>
            </tr>
            <tr>
                <td style="width: 100px">
                    ${ui.includeFragment("kenyaui", "widget/field", [object: command, property: "listingDate"])}
                </td>
            </tr>
        </table>
    </fieldset>
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
                    <td style="width: 140px">
                    <label class="ke-field-label">Marital Status</label>
                    <select name="maritalStatus" id="maritalStatus">
                        <option></option>
                        <% maritalStatusOptions.each { %>
                        <option ${
                                (command.maritalStatus == null) ? "" : it.value == command.maritalStatus ? "selected" : ""}
                                value="${it.value}">${it.label}</option>
                        <% } %>
                    </select>

                    </td>
                </tr>
            </table>

        </fieldset>

        <fieldset>
            <legend>Contact</legend>

            <% addressRows.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>

        </fieldset>

        <fieldset>
            <legend>Relationship</legend>
            <table>
                <tr>
                    <td class="ke-field-label">Relationship to Patient*</td>
                    <td class="ke-field-label">Living with Client?</td>
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
                </tr>
            </table>
        </fieldset>

        <fieldset class="ipvQuestions">
            <legend>IPV Questions</legend>
            <table>
                <tr>
                    <td>
                        <label class="ke-field-label">1. Has he/she ever hit, kicked, slapped, or otherwise physically hurt you?</label>
                    </td><td>
                    <span class="ke-field-content">
                        <input type="radio" name="physicalAssault" class="ipv" value="1065"/> Yes
                        <input type="radio" name="physicalAssault" class="ipv" value="1066"/> No
                    </span>
                </td>
                </tr>
                <tr>
                    <td>
                        <label class="ke-field-label">2. Has he/she ever threatened to hurt you?</label>
                    </td><td>
                    <span class="ke-field-content">
                        <input type="radio" name="threatened" class="ipv" value="1065"/> Yes
                        <input type="radio" name="threatened" class="ipv" value="1066"/> No
                    </span>
                </td>
                </tr>
                <tr>
                    <td>
                        <label class="ke-field-label">3.Has he/she ever forced you to do something sexually that made you feel uncomfortable?</label>
                    </td><td>
                    <span class="ke-field-content">
                        <input type="radio" name="sexualAssault" class="ipv" value="1065"/> Yes
                        <input type="radio" name="sexualAssault" class="ipv" value="1066"/> No
                    </span>
                </td>
                </tr>
            </table>
        </fieldset>
        <fieldset class="ipvOutcome">
            <legend>IPV Outcome</legend>
            <table>
                <tr>
                    <td class="ke-field-label">IPV Outcome</td>
                </tr>
                <tr>
                    <td>
                        <select name="ipvOutcome" id="ipvOutcome">
                            <option></option>
                            <% ipvOutcomeOptions.each { %>
                            <option ${(command.ipvOutcome == null) ? "" : it == command.ipvOutcome ? "selected" : ""}
                                    value="${it}">${it}</option>
                            <% } %>
                        </select>
                    </td>
                </tr>
            </table>

        </fieldset>

        <fieldset>
            <legend>Baseline Information</legend>
            <table>
                <tr>
                    <td class="ke-field-label">HIV Status</td>
                     <td id="tdReportedTestDate" class="ke-field-label">Reported test date</td>
                     <td class="ke-field-label">Booking Date</td>
                    <td class="ke-field-label">Preferred PNS Approach</td>
                </tr>
                <tr>
                    <td style="width: 140px">
                        <select name="baselineHivStatus" id="baselineHivStatus">
                            <option></option>
                            <% hivStatusOptions.each { %>
                            <option ${
                                    (command.baselineHivStatus == null) ? "" : it == command.baselineHivStatus ? "selected" : ""}
                                    value="${it}">${it}
                            </option>
                            <% } %>
                        </select>
                    </td>
                    <td id="reportedTestDate" style="width: 200px">
                        ${ui.includeFragment("kenyaui", "widget/field", [object: command, property: "reportedTestDate"])}
                    </td>
                    <td style="width: 200px">
                        ${ui.includeFragment("kenyaui", "widget/field", [object: command, property: "appointmentDate"])}
                    </td>

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
        const YES_CONCEPT_ID = 1065;
        const NO_CONCEPT_ID = 1066;
        var assessmentYesResponses = [];
        var assessmentNoResponses = [];
        const TOTAL_RESPONSES = 3;

        // Add event listener for opening and closing details
        jQuery('.ipv').on('click', function () {
            var radio = jQuery(this).closest('input[type=radio]');
            var value = radio.val();
            if (value == YES_CONCEPT_ID && assessmentYesResponses.length <= TOTAL_RESPONSES) {

                assessmentYesResponses.push(value);
            }
            if (value == NO_CONCEPT_ID && assessmentNoResponses.length <= TOTAL_RESPONSES) {

                assessmentNoResponses.push(value);
            }

            if (value == NO_CONCEPT_ID && assessmentYesResponses.length <= TOTAL_RESPONSES) {

                assessmentYesResponses.pop();
            }

            if (assessmentYesResponses.length == 0 && assessmentNoResponses.length == 0) {
                alert("Please select an IPV question")
            } else {
                if (assessmentYesResponses.length > 0) {

                    jQuery('#ipvOutcome').val("True");

                } else {
                    jQuery('#ipvOutcome').val("False");

                }
            }

        });

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
        jq('.ipvQuestions').hide();
        jq('.ipvOutcome').hide();
        jq('#reportedTestDate').hide();
        jq('#tdReportedTestDate').hide();
        jq("select[name='relationType']").change(function () {

            var relType = jq(this).val();

            console.log('Relationship type ' + relType);

            if (relType === "5617" || relType === "163565"/* Spouse or Partner*/) {

                jq('.ipvQuestions').show();
                jq('.ipvOutcome').show();
            }
            else {
                jq('.ipvQuestions').hide();
                jq('.ipvOutcome').hide();
            }
        });

    }); // end of jQuery initialization block

    function updateBirthdate(data) {
        var birthdate = new Date(data.birthdate);
        kenyaui.setDateField('patient-birthdate', birthdate);

    }
    jq("select[name='baselineHivStatus']").change(function () {

        var hivStatus = jq(this).val();

        if (hivStatus === "Positive" || hivStatus === "Negative") {

            jq('#reportedTestDate').show();
            jq('#tdReportedTestDate').show();
        }
        else {
            jq('#reportedTestDate').hide();
            jq('#tdReportedTestDate').hide();
        }
    });

</script>
