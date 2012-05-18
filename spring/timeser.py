from chartit import DataPool, Chart

def edname(ed_num):
    names = {3:'Ed3', 9:'Ed9', 11:'Ed11', 15:'Ed15', 1000:'Wiki'}
    return names[ed_num]

def prep_time_series_chart(models):
    series = []
    terms = {}
    for m in models:
        name = m.all()[0].match_master.name
        mnames = {}
        for art in m.all():
            mnames[art.name]='vscore'

        terms[name+'_ed'] = mnames.keys()
        series.append(
            {'options': { 'source': m.all() },
             'terms': [{name+'_ed':'art_ed'},
                       mnames]
             })
    
    objdata = DataPool(series=series)
                
    chart = Chart(
        datasource = objdata,
        series_options = [
            {'options': {
                    'type': 'line',
                    'stacking': False,
                    },
             
             'terms': terms,

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

