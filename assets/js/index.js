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
    if (!apiDataToSet) {
        var today = new Date();
        var beginDate = today.toISOString().slice(0,10).replace(/-/g,"");
        apiDefaults = {
            begin_date: beginDate,
            range: "48",
            station: "8665530",
            product: "predictions",
            datum: "STND",
            time_zone: "lst_ldt",
            units: "english",
            format: "json"
        }
    } else {

    }
    
}