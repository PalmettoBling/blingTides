/*
const baseUri = 'https://api.tidesandcurrents.noaa.gov/api/prod/datagetter?';
*/

function graphTides() {
    const apiUri = `https://api.tidesandcurrents.noaa.gov/api/prod/datagetter?station=8665530&date=latest&product=predictions&datum=STND&time_zone=lst_ldt&units=english&format=json`;
    
    fetch(apiUri)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            let data = response.json();
            let responseData = data.predictions;
            
            const ctx = document.getElementById('tidesGraph');

            new Chart(ctx, {
                type: 'line',
                data: {
                    datasets: [{
                        data: predictionData
                    }]
                },
                options: {
                    parsing: {
                        xAxisKey: 't',
                        yAxisKey: 'v'
                    }
                }
            });
        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });
}

export { graphTides };