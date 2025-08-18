import { graphTides } from "./graphTides.js";

const apiUri = `https://api.tidesandcurrents.noaa.gov/api/prod/datagetter?date=latest&station=8665530&product=predictions&datum=STND&time_zone=lst_ldt&units=english&format=json`;
                
fetch(apiUri)
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