var tideChart;

export function graphTides(predictionData) {
    const tidesGraph = document.getElementById("tidesGraph");
    if (typeof tideChart !== "undefined") {
        tideChart.destroy();
    }

    tideChart = new Chart(tidesGraph, {
        type: "line",
        data: {
            datasets: [
                {
                    label: "Tide Predictions",
                    tension: 1,
                    data: predictionData
                }
            ]
        },
        options: {
            parsing: {
                xAxisKey: "t",
                yAxisKey: "v"
            }
        }
    });
}
