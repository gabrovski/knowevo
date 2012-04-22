from chartit import DataPool, Chart

def prep_time_series_chart(models):
    objdata = DataPool(
        series = [
            {'options': { 'source': models },
             'terms': ['art_ed', 'volume_score', 'name'] }])
    
    chart = Chart(
        datasource = objdata,
        series_options = [
            {'options': {
                    'type': 'line',
                    'stacking': False,
                    },
             
             'terms': {
                    'art_ed': [
                        'volume_score'],
                    },

             }],

        chart_options = {
            'title': {
                'text': 'Importance through time'
                },
            'xAxis': {
                'title': {
                    'text': 'Edition number'
                    },
                },
            })

    return chart

