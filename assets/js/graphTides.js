export function graphTides(predictionData) {
    const tidesGraph = document.getElementById('tidesGraph');
    new Chart(tidesGraph, {
        type: 'line',
        data: {
            datasets: [{
                label: 'Tide Predictions',
                tension: 1,
                data: predictionData
            }]
        },
        options: {
            parsing: {
                // need to check this key for if it can change to "time" from this page: https://www.chartjs.org/docs/latest/axes/
                xAxisKey: 't',
                yAxisKey: 'v'
            }
        }
    });
};