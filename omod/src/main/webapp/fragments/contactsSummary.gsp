<%
    ui.decorateWith("kenyaui", "panel", [ heading: "Contacts History" ])
%>
<style>
.simple-table {
    border: solid 1px #DDEEEE;
    border-collapse: collapse;
    border-spacing: 0;
    font: normal 13px Arial, sans-serif;
}
.simple-table thead th {
    background-color: #DDEFEF;
    border: solid 1px #DDEEEE;
    color: #336B6B;
    padding: 10px;
    text-align: left;
    text-shadow: 1px 1px 1px #fff;
}
.simple-table td {
    border: solid 1px #DDEEEE;
    color: #333;
    padding: 5px;
    text-shadow: 1px 1px 1px #fff;
}
</style>


<div>

    <fieldset>
        <legend>Contacts History</legend>
        <%if (contactsDetails) { %>
        <table class="simple-table">

        <tr>
            <th align="left">Total contacts</th>
            <th align="left">Total traced</th>
        </tr>
            <% contactsDetails.each { %>
            <tr>
                <td>20</td>
                <td>10</td>

            </tr>
            <% } %>
        </table>
        <% } else {%>
        <div>No linkage history</div>

        <% } %>
    </fieldset>

</div>
