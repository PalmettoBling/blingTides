import { graphTides } from "./graphTides.js";

const tidesPredictionUri = new URL("https://api.tidesandcurrents.noaa.gov/api/prod/datagetter");
let today = new Date();
let begin_date = today.toISOString().slice(0,10).replace(/-/g,"");
craftApiUri({"begin_date": begin_date});

// Function to construct the API URI with default parameters
// If parameters are not provided, they will be set to default values
// IF NOT DEFINED, SET DEFAULTS
// IF DEFINED, SET TO THOSE VALUES
function craftApiUri({begin_date, range="48", station="8665530", product="predictions", datum="STND", time_zone="lst_ldt", units="english", format="json"} = {}) {
    // Need to verify edge conditions for date changeover at UTC midnight
    // Need to verify edge conditions for API call data if not passed default, would resetting it to default cause issues?

    //needs to SET values, not just append if already set
    tidesPredictionUri.searchParams.set("begin_date", begin_date);
    tidesPredictionUri.searchParams.set("range", range);
    tidesPredictionUri.searchParams.set("station", station);
    tidesPredictionUri.searchParams.set("product", product);
    tidesPredictionUri.searchParams.set("datum", datum);
    tidesPredictionUri.searchParams.set("time_zone", time_zone);
    tidesPredictionUri.searchParams.set("units", units);
    tidesPredictionUri.searchParams.set("format", format);
    graphTidesApiCall(tidesPredictionUri);
}

// Function to call the API and handle the response
// Fetch the data from the API and pass it to the graphing function
// INPUT: URL object with the API endpoint and query parameters
// OUTPUT: Calls graphTides with the prediction data, which updates page with chart from Chart.js
function graphTidesApiCall(tidesPredictionUri) {
    fetch(tidesPredictionUri)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return response.json();
        })
        .then(data => {
            let predictionData = data.predictions;
            
            graphTides(predictionData);
        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });
}

// Event listener for date input change
// When the date input changes, update the API URI and fetch new data, then update the chart
document.getElementById("tideDate").addEventListener("input", function() {
    let selectedDate = new Date(this.value);
    let formattedDate = selectedDate.toISOString().slice(0,10).replace(/-/g,"");    
    craftApiUri({"begin_date": formattedDate});
});