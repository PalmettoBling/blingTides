import Chart from 'https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.js'

const baseUri = `https://api.tidesandcurrents.noaa.gov/api/prod/datagetter`;

function tidesCall() {
    const apiDetails = `station=8665530&date=latest&product=predictions&datum=STND&time_zone=lst_ldt&units=english&format=json`;
    const url = `${baseUri}?${apiDetails}`;
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        });
}

function graphTides() {
    tidesCall()
        .then(data => {
            let predictionData = data.predictions;
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
            })

        })
        .catch(error => console.error('Error fetching tide data:', error));
}