from chartit import DataPool, Chart

def edname(ed_num):
    names = {3:'Ed3', 9:'Ed9', 11:'Ed11', 15:'Ed15', 1000:'Wiki'}
    return names[ed_num]

def prep_time_series_chart(models):
    objdata = DataPool(
        series = [
            {'options': { 'source': models },
             'terms': ['art_ed', 'vscore', 'name'] }])
    
    chart = Chart(
        datasource = objdata,
        series_options = [
            {'options': {
                    'type': 'line',
                    'stacking': False,
                    },
             
             'terms': {
                    'art_ed': [
                        'vscore'],
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
            },
        x_sortf_mapf_mts = (None, edname, False))

    return chart

