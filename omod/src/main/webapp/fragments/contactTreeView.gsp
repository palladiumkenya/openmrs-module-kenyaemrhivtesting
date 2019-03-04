<%
    ui.includeCss("hivtestingservices", "TreantJs/Treant.css")
    ui.includeCss("hivtestingservices", "TreantJs/collapsable.css")
    ui.includeCss("hivtestingservices", "TreantJs/vendor/perfect-scrollbar/perfect-scrollbar.css")

    ui.includeJavascript("hivtestingservices", "TreantJs/vendor/raphael.js")
    ui.includeJavascript("hivtestingservices", "TreantJs/Treant.js")
    ui.includeJavascript("hivtestingservices", "TreantJs/jquery.easing.js")
    ui.includeJavascript("hivtestingservices", "TreantJs/collapsable.js")

    def male = ui.resourceLink("kenyaui', 'images/buttons/patient_m.png")
    def female = ui.resourceLink("kenyaui', 'images/buttons/patient_f.png")



%>

<div class="chart" id="collapsable-example"></div>

<script type="text/javascript">

    //On ready
    jQuery(function () {
        var chart_config = {
            chart: {
                container: "#collapsable-example",
                animateOnInit: true,
                rootOrientation:  'NORTH',
                siblingSeparation:   20,
                subTeeSeparation:    60,
                node: {
                    collapsable: true,
                    HTMLclass: 'nodeExample1'
                },
                animation: {
                    nodeAnimation: "easeOutBounce",
                    nodeSpeed: 700,
                    connectorsAnimation: "bounce",
                    connectorsSpeed: 700
                },
                connectors: {
                    type: 'step',
                    style: {
                        "stroke-width": 2,
                        "stroke-linecap": "round",
                        "stroke": "#ccc"
                    }
                },
                scrollbar: "None"
            },
            nodeStructure: ${sresponse}
        };

        tree = new Treant( chart_config );



    }); // end of jQuery initialization bloc


</script>