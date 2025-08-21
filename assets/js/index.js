import { graphTides } from "./graphTides.js";


const tidesPredictionUri = new URL("https://api.tidesandcurrents.noaa.gov/api/prod/datagetter");
let today = new Date();
let beginDate = today.toISOString().slice(0,10).replace(/-/g,"");
craftApiUri({"beginDate": beginDate});

/*fetch(tidesPredictionUri)
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
    });*/

function craftApiUri(apiRequestData) {
    console.log("Base URI:", tidesPredictionUri.toString());
    console.log("Crafting API URL with data:", apiRequestData);)
    tidesPredictionUri.searchParams.set(apiRequestData);
    console.log("Crafted API URL:", tidesPredictionUri.toString());
    return;
}

// IF NOT DEFINED, SET DEFAULTS
// IF DEFINED, SET TO THOSE VALUES
function craftApiUri({beginDate, range="48", station="8665530", product="predictions", datum="STND", time_zone="lst_ldt", units="english", format="json"} = {}) {

    console.log("Begin Date set to:", beginDate);
    
    // Need to verify edge conditions for date changeover at UTC midnight
    // Need to verify edge conditions for API call data if not passed default, would resetting it to default cause issues?


    /* Replacing with URL object vs string concatenation and data object
    apiDefaults.begin_date = apiDataToSet.begin_date ?? beginDate;
    apiDefaults.range = apiDataToSet.range ?? "48";
    apiDefaults.station = apiDataToSet.station ?? "8665530";
    apiDefaults.product = apiDataToSet.product ?? "predictions";
    apiDefaults.datum = apiDataToSet.datum ?? "STND";
    apiDefaults.time_zone = apiDataToSet.time_zone ?? "lst_ldt";
    apiDefaults.units = apiDataToSet.units ?? "english";
    apiDefaults.format = apiDataToSet.format ?? "json";
    */
}