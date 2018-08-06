<fieldset>
    <legend>Contact Profile</legend>

    <div>
        <div style="float: left; width: 35%;">

            <div>
                <span class="ke-identifier-type">Contact Name:</span>
                <span class="ke-identifier-value">${patientContact.fullName}</span>
            </div>

            <div>
                <span class="ke-identifier-type">Relationship:</span>
                <span class="ke-identifier-value">${patientContact.relationType}</span>
            </div>

            <div>
                <span class="ke-identifier-type">Gender:</span>
                <span class="ke-identifier-value">${patientContact.sex}</span>
            </div>

            <div>
                <span class="ke-identifier-type">Age:</span>
                <span class="ke-identifier-value">${patientContact.age} <small>(${patientContact.birthDate})</small>
                </span>
            </div>

            <div>
                <span class="ke-identifier-type">Marital Status:</span>
                <span class="ke-identifier-value">${patientContact.maritalStatus}
                </span>
            </div>

            <div>
                <span class="ke-identifier-type">Living with Index:</span>
                <span class="ke-identifier-value">${patientContact.livingWithPatient}
                </span>
            </div>


        </div>

        <div style="float: left; width: 30%; text-align: left">
            <div>
                <span class="ke-identifier-type">Physical Address:</span>
                <span class="ke-identifier-value">${patientContact.physicalAddress}</span>
            </div>

            <div>
                <span class="ke-identifier-type">Phone Contact:</span>
                <span class="ke-identifier-value">${patientContact.phoneContact}</span>
            </div>
        </div>

        <div style="float: left; width: 30%; text-align: left">
            <div>
                <span class="ke-identifier-type">Baseline HIV Status:</span>
                <span class="ke-identifier-value">${patientContact.baselineHivStatus}</span>
            </div>

            <div>
                <span class="ke-identifier-type">Appointment Date:</span>
                <span class="ke-identifier-value">${patientContact.appointmentDate}</span>
            </div>

            <div>
                <span class="ke-identifier-type">PNS Approach:</span>
                <span class="ke-identifier-value">${patientContact.pnsApproach}</span>
            </div>

        </div>
    </div>
</fieldset>

<div style="clear: both; height: 5px;"></div>