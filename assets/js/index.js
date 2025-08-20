import { graphTides } from "./graphTides.js";

var apiDefaults = {};
setApiData();

fetch(craftApiUrl(apiDefaults))
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

function craftApiUrl(apiRequestData) {
    let baseUri = "https://api.tidesandcurrents.noaa.gov/api/prod/datagetter?";
    let datafulUri = baseUri + new URLSearchParams(apiRequestData).toString();
    console.log("Crafted API URL:", datafulUri);
    return datafulUri;
}

function setApiData(apiDataToSet) {
    var today = new Date();
    var beginDate = today.toISOString().slice(0,10).replace(/-/g,"");

    // Need to verify edge conditions for date changeover at UTC midnight
    // Need to verify edge conditions for API call data if not passed default, would resetting it to default cause issues?
    apiDefaults.begin_date = apiDataToSet.begin_date ?? beginDate;
    apiDefaults.range = apiDataToSet.range ?? "48";
    apiDefaults.station = apiDataToSet.station ?? "8665530";
    apiDefaults.product = apiDataToSet.product ?? "predictions";
    apiDefaults.datum = apiDataToSet.datum ?? "STND";
    apiDefaults.time_zone = apiDataToSet.time_zone ?? "lst_ldt";
    apiDefaults.units = apiDataToSet.units ?? "english";
    apiDefaults.format = apiDataToSet.format ?? "json";
}