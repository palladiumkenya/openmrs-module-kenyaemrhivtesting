<%
	ui.decorateWith("kenyaemr", "standardPage")
%>
<div class="ke-page-content">
	${ ui.includeFragment("hivtestingservices", "registerContact", [ patientContact: patientContact, returnUrl: returnUrl ]) }
</div>