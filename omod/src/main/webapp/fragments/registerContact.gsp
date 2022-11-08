<%
    ui.decorateWith("kenyaui", "panel", [heading: "Register Patient Contact", frameOnly: true])

    def nameFields = [
            [
                    [object: command, property: "personName.familyName", label: "Surname *"],
                    [object: command, property: "personName.givenName", label: "First name *"],
                    [object: command, property: "personName.middleName", label: "Other name(s)"]
            ],
    ]

    def otherDemogFieldRows = [
            [
                    [object: command, property: "maritalStatus", label: "Marital status *", config: [style: "list", options: maritalStatusOptions]],
                    [object: command, property: "occupation", label: "Occupation *", config: [style: "list", options: occupationOptions]],
                    [object: command, property: "education", label: "Education *", config: [style: "list", options: educationOptions]]
            ]
    ]
    def deathFieldRows = [
            [
                    [object: command, property: "dead", label: "Deceased"],
                    [object: command, property: "deathDate", label: "Date of death"]
            ]
    ]

    def nextOfKinFieldRows = [
            [
                    [object: command, property: "nextOfKinContact", label: "Phone Number"],
                    [object: command, property: "nextOfKinAddress", label: "Postal Address"]
            ]
    ]

    def contactsFields = [
            [
                    [object: command, property: "telephoneContact", label: "Telephone contact *"]
            ],
            [
                    [object: command, property: "alternatePhoneContact", label: "Alternate phone number"],
                    [object: command, property: "personAddress.address1", label: "Postal Address", config: [size: 60]],
                    [object: command, property: "emailAddress", label: "Email address"]
            ]
    ]

    def locationSubLocationVillageFields = [

            [
                    [object: command, property: "personAddress.address6", label: "Location"],
                    [object: command, property: "personAddress.address5", label: "Sub-location"],
                    [object: command, property: "personAddress.cityVillage", label: "Village *"]
            ]
    ]

    def landmarkNearestFacilityFields = [

            [
                    [object: command, property: "personAddress.address2", label: "Landmark"],
                    [object: command, property: "nearestHealthFacility", label: "Nearest Health Center"]
            ]
    ]
%>

<script type="text/javascript" src="/${ contextPath }/moduleResources/hivtestingservices/scripts/KenyaAddressHierarchy.js"></script>
<script type="text/javascript" src="/${ contextPath }/moduleResources/hivtestingservices/scripts/upiVerificationUtils.js"></script>

<form id="edit-patient-form" method="post" action="${ui.actionLink("hivtestingservices", "registerContact", "savePatient")}">
    <% if (command.original) { %>
    <input type="hidden" name="patientRelatedTo" value="${patientRelatedTo.patientId}"/>
    <input type="hidden" name="patientContact" value="${patientContact.id}"/>
    <% } %>

    <div class="ke-panel-content">

        <div class="ke-form-globalerrors" style="display: none"></div>

        <div class="ke-form-instructions">
            <strong>*</strong> indicates a required field
        </div>

        <fieldset>
            <legend>ID Numbers</legend>

            <table>
                <tr>
                    <td class="ke-field-label">Patient Clinic Number</td>
                    <td>${ui.includeFragment("kenyaui", "widget/field", [object: command, property: "patientClinicNumber"])}</td>
                    <td class="ke-field-instructions"><% if (!command.patientClinicNumber) { %>(if available)<%
                        } %></td>
                </tr>
                <tr>
                    <td class="ke-field-label">National ID Number</td>
                    <td>${ui.includeFragment("kenyaui", "widget/field", [object: command, property: "nationalIdNumber"])}</td>
                    <td class="ke-field-instructions"><% if (!command.nationalIdNumber) { %>(If the patient is below 18 years of age, enter the guardian`s National Identification Number if available.)<% } %></td>
                </tr>
            </table>

        </fieldset>

        <fieldset>
            <legend>Demographics</legend>

            <% nameFields.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>

            <table>
                <tr>
                    <td valign="top">
                        <label class="ke-field-label">Sex *</label>
                        <span class="ke-field-content">
                            <input type="radio" name="gender" value="F"
                                   id="gender-F" ${command.gender == 'F' ? 'checked="checked"' : ''}/> Female
                            <input type="radio" name="gender" value="M"
                                   id="gender-M" ${command.gender == 'M' ? 'checked="checked"' : ''}/> Male
                            <span id="gender-F-error" class="error" style="display: none"></span>
                            <span id="gender-M-error" class="error" style="display: none"></span>
                        </span>
                    </td>
                    <td valign="top"></td>
                    <td valign="top">
                        <label class="ke-field-label">Date of Birth *</label>
                        <span class="ke-field-content">
                            ${ui.includeFragment("kenyaui", "widget/field", [id: "patient-birthdate", object: command, property: "birthdate"])}
                            <span id="patient-birthdate-estimated">
                                <input type="radio" name="birthdateEstimated"
                                       value="true" ${command.birthdateEstimated ? 'checked="checked"' : ''}/> Estimated
                                <input type="radio" name="birthdateEstimated"
                                       value="false" ${!command.birthdateEstimated ? 'checked="checked"' : ''}/> Exact
                            </span>
                            &nbsp;&nbsp;&nbsp;

                            <span id="from-age-button-placeholder"></span>
                        </span>
                    </td>
                </tr>
            </table>

            <% otherDemogFieldRows.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>
            <% deathFieldRows.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>

        </fieldset>

    </fieldset>

        <fieldset>
            <legend>Address</legend>

            <table>
            <tr>
            <td class="ke-field-label">Country *</td>
            <td> </td>
            </tr>
            <tr>
                
                <td>${ui.includeFragment("kenyaui", "widget/field", [object: command, property: "country", id: "country-registration", config: [style: "list", options: countryOptions]])}</td>
                <td> <input type="checkbox" name="select-kenya-option" value="Y" id="select-kenya-option" /> Select Kenya </td>
                <td>
                    <div id="country-msgBox" class="ke-warning">Country is Required</div>
                </td>
            </tr>
            </table>

            <% contactsFields.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>

            <table>
                <tr>
                    <td class="ke-field-label" style="width: 265px">County *</td>
                    <td class="ke-field-label" style="width: 260px">Sub-County *</td>
                    <td class="ke-field-label" style="width: 260px">Ward *</td>
                </tr>

                <tr>
                    <td style="width: 265px">
                        <select id="county" name="personAddress.countyDistrict">
                            <option></option>
                            <%countyList.each { %>
                            <option value="${it}">${it}</option>
                            <%}%>
                        </select>
                    </td>
                    <td style="width: 260px">
                        <select id="subCounty" name="personAddress.stateProvince">
                            <option></option>
                        </select>
                    </td>
                    <td style="width: 260px">
                        <select id="ward" name="personAddress.address4">
                            <option></option>
                        </select>
                    </td>
                </tr>

                <tr>
                    <td>
                        <div id="county-msgBox" class="ke-warning">County is Required</div>
                    </td>
                    <td>
                        <div id="subCounty-msgBox" class="ke-warning">Sub County is Required</div>
                    </td>
                    <td>
                        <div id="ward-msgBox" class="ke-warning">Ward is Required</div>
                    </td>
                </tr>
            </table>
            <% locationSubLocationVillageFields.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>

            <% landmarkNearestFacilityFields.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>
        </fieldset>

        <fieldset>
            <legend>Next of Kin Details</legend>
            <table>
                <tr>
                    <td class="ke-field-label" style="width: 260px">Name</td>
                    <td class="ke-field-label" style="width: 260px">Relationship</td>
                </tr>

                <tr>
                    <td style="width: 260px">${ui.includeFragment("kenyaui", "widget/field", [object: command, property: "nameOfNextOfKin"])}</td>
                    <td style="width: 260px">
                        <select name="nextOfKinRelationship">
                            <option></option>
                            <%nextOfKinRelationshipOptions.each { %>
                            <option value="${it}">${it}</option>
                            <%}%>
                        </select>
                    </td>
                </tr>
            </table>
            <% nextOfKinFieldRows.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>

        </fieldset>

    </div>

    <div class="text-wrap" align="center" id="post-msgBox"></div>
    <br/>

    <div class="ke-panel-footer">
        <div class="message-nupi-reminder message-colors">
            <label id="msgBox">Please Don't Forget To Verify With Client Registry</label>
        </div>
        <button type="submit">
            <img src="${ui.resourceLink("kenyaui", "images/glyphs/ok.png")}"/> ${command.original ? "Save Changes" : "Create Patient"}
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

<style>
.message-nupi-reminder {
    margin-right: 5px;
    margin-left: 5px;
}

.message-colors {
    padding: 10px 20px;
    background-color: yellowgreen;
    color: #000000;
    font-weight: bold;
}
</style>

<script type="text/javascript">
    //On ready
    jQuery(function () {
        var countyCode;

        jQuery("#country-msgBox").hide();
        jQuery("#county-msgBox").hide();
        jQuery("#subCounty-msgBox").hide();
        jQuery("#ward-msgBox").hide();

        jQuery('#from-age-button').appendTo(jQuery('#from-age-button-placeholder'));

        jQuery('#edit-patient-form .cancel-button').click(function () {
            ui.navigate('${ config.returnUrl }');
        });

        kenyaui.setupAjaxPost('edit-patient-form', {
            onSuccess: function (data) {
                if (data.id) {
                    ui.navigate('kenyaemr', 'registration/registrationViewPatient', {patientId: data.id});
                } else {
                    kenyaui.notifyError('Saving patient was successful, but unexpected response');
                }
            }
        });

        jQuery('#county').change(updateSubcounty);
        jQuery('#subCounty').change(updateWard);
        jQuery('#select-kenya-option').click(selectCountryKenyaOptionOnRegistration);

        function validateFields() {
            //County Code status:
            //var countyCode;
            if(jQuery('select[name="personAddress.countyDistrict"]').val() !=""){
                jQuery("#county-msgBox").hide();
                countyCode = countyObject[jQuery('select[name="personAddress.countyDistrict"]').val()].countyCode;
            } else {
                // County is required
                jQuery("#post-msgBox").text("Please enter county to successfully register patient");
                jQuery("#post-msgBox").show();
                jQuery("#county-msgBox").show();
                return(false);
            }
            //SubCounty Validation
            if(jQuery('select[name="personAddress.stateProvince"]').val() ==""){
                // Sub-County is required
                jQuery("#post-msgBox").text("Please enter sub county to successfully register patient");
                jQuery("#post-msgBox").show();
                jQuery("#subCounty-msgBox").show();
                return(false);
            }else{
                jQuery("#subCounty-msgBox").hide();
            }
            //Ward Validation
            if(jQuery('select[name="personAddress.address4"]').val() ==""){
                //Ward is required
                jQuery("#post-msgBox").text("Please enter ward to successfully register patient");
                jQuery("#post-msgBox").show();
                jQuery("#ward-msgBox").show();
                return(false);
            }else{
                jQuery("#ward-msgBox").hide();
            }
            //Telephone Validation
            if(jQuery('input[name="telephoneContact"]').val() ==""){
                // Telephone number is required
                jQuery("#post-msgBox").text("Please enter telephone number to successfully post to CR");
                jQuery("#post-msgBox").show();
                jQuery("#phone-msgBox").show();
                return(false);
            }else{
                jQuery("#phone-msgBox").hide();
            }
            //Age Validation
            if(jQuery('#patient-birthdate_date').val() ==""){
                // Age is required
                jQuery("#post-msgBox").text("Please enter age to successfully post to CR");
                jQuery("#post-msgBox").show();
                jQuery("#age-msgBox").show();
                return(false);
            }else{
                jQuery("#age-msgBox").hide();
            }
            //First name Validation
            if(jQuery('input[name="personName.givenName"]').val() ==""){
                // First Name is required
                jQuery("#post-msgBox").text("Please enter First name to successfully post to CR");
                jQuery("#post-msgBox").show();
                jQuery('#firstname-msgBox').show();
                return(false);
            }else{
                jQuery('#firstname-msgBox').hide();
            }
            //Surname Validation
            if(jQuery('input[name="personName.familyName"]').val() ==""){
                //Family Name is required
                jQuery("#post-msgBox").text("Please enter Surname to successfully post to CR");
                jQuery("#post-msgBox").show();
                jQuery('#surname-msgBox').show();
                return(false);
            }else{
                jQuery('#surname-msgBox').hide();
            }
            //Village Validation
            if(jQuery('input[name="personAddress.cityVillage"]').val() =="") {
                //Village is required
                jQuery("#post-msgBox").text("Please enter Village to successfully post to CR");
                jQuery("#post-msgBox").show();
                jQuery('#village-msgBox').show();
                return(false);
            }else{
                jQuery('#village-msgBox').hide();
            }

            return(true);
        }

        function updateSubcounty() {
            jQuery('#subCounty').empty();
            jQuery('#ward').empty();
            var selectedCounty = jQuery('#county').val();
            var scKey;
            jQuery('#subCounty').append(jQuery("<option></option>").attr("value", "").text(""));
            for (scKey in kenyaAddressHierarchy[selectedCounty]) {
                jQuery('#subCounty').append(jQuery("<option></option>").attr("value", scKey).text(scKey));
            }
        }

        function updateWard() {
            jQuery('#ward').empty();
            var selectedCounty = jQuery('#county').val();
            var selectedsubCounty = jQuery('#subCounty').val();
            var scKey;
            jQuery('#ward').append(jQuery("<option></option>").attr("value", "").text(""));
            for (scKey in kenyaAddressHierarchy[selectedCounty][selectedsubCounty]) {
                jQuery('#ward').append(jQuery("<option></option>").attr("value", kenyaAddressHierarchy[selectedCounty][selectedsubCounty][scKey].facility).text(kenyaAddressHierarchy[selectedCounty][selectedsubCounty][scKey].facility));
            }
        }

    }); // end of jQuery initialization block

    function updateBirthdate(data) {
        var birthdate = new Date(data.birthdate);
        kenyaui.setDateField('patient-birthdate', birthdate);
        kenyaui.setRadioField('patient-birthdate-estimated', 'true');
    }

    //Ckeckbox to select country Kenya on registration
    var selectCountryKenyaOptionOnRegistration = function () {
        console.log("Reg country selection");
        var val = jq(this).val();
        if (jq(this).is(':checked')){
            jQuery('select[id=country-registration]').val(162883);
        }else{
            jQuery('select[id=country-registration]').val("");
        }

        jQuery('select[id=country-registration]').on('change', function() {
         if(this.value != 162883)  {
             jq("#select-kenya-option").prop("checked", false);
         }
         });
    }

</script>