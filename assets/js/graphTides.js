// NEED TO ADD ABILITY TO UPDATE CHART WHEN DATE IS CHANGED
// constant chart outside function to avoid re-creating chart on each update? Or have a reference to destroy to update it?
var tideChart;

export function graphTides(predictionData) {
    const tidesGraph = document.getElementById('tidesGraph');
    if (typeof tideChart !== 'undefined') {
        tideChart.destroy();
    }
    tideChart = new Chart(tidesGraph, {
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