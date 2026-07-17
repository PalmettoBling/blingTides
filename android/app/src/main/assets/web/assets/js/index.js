import { graphTides } from "./graphTides.js";

const tidesPredictionUri = new URL("https://api.tidesandcurrents.noaa.gov/api/prod/datagetter");
let today = new Date();
let begin_date = today.toISOString().slice(0, 10).replace(/-/g, "");
craftApiUri({ begin_date: begin_date });

function craftApiUri({
    begin_date,
    range = "48",
    station = "8665530",
    product = "predictions",
    datum = "STND",
    time_zone = "lst_ldt",
    units = "english",
    format = "json"
} = {}) {
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

function graphTidesApiCall(uri) {
    fetch(uri)
        .then((response) => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return response.json();
        })
        .then((data) => {
            let predictionData = data.predictions;
            graphTides(predictionData);
        })
        .catch((error) => {
            console.error("Error fetching data:", error);
        });
}

document.getElementById("tideDate").addEventListener("input", function () {
    let selectedDate = new Date(this.value);
    let formattedDate = selectedDate.toISOString().slice(0, 10).replace(/-/g, "");
    craftApiUri({ begin_date: formattedDate });
});
