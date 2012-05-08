try:
    import os
    os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'
    from incunabula.models import Article, MasterArticle
except:
    print 'django settings not loaded'
    pass


import serialize as ser
import bisect


###check if articles is to be flagged
def is_valid_article(ed, birth):
    prev = 30
    return (
        (ed == 3 and birth < 1797-prev) or
        (ed == 9 and birth < 1875-prev) or
        (ed == 11 and birth < 1910-prev) or
        (ed == 15))


def is_missing(ed, name):
    try:
        art = Article.objects.get(match_master__name=name, art_ed=ed)
        return False
    except:
        #raise
        return True


###retrieve bad articles
def get_missing_articles(top):
    missing = {3:[],9:[],11:[],15:[]}
    
    f = open(top)
    for line in f:
        splits = line.strip().split(' ')
        name, birth = splits[0], int(splits[1])
            
        if is_valid_article(3, birth) and is_missing(3, name):
            missing[3].append(name)
            
        if is_valid_article(9, birth) and is_missing(9, name):
            missing[9].append(name)

        if is_valid_article(11, birth) and is_missing(11, name):
            missing[11].append(name)

        if is_valid_article(15, birth) and is_missing(15, name):
            missing[15].append(name)
                
    f.close()

    for ed in [3, 9, 11, 15]:
        for m in missing[ed]:
            print m, ed
        

def get_edition_matches(ed):
    for art in Article.objects.filter(art_ed=ed).iterator():
        print art.name    


###candid generation
class CandidDealer:
    def __init__(self, exist_brit_path, exist_wiki_path):
        self.existing_brit = CandidDealer.load_existing_brit_names(exist_brit_path)
        self.existing_wiki = CandidDealer.load_exisiting_wiki_names(exist_wiki_path)


    ###helper functions
    @staticmethod
    def get_candids(wname, existing):
        index = bisect.bisect_left(wname, exisiting)
        return index], index+1
    
    @staticmethod
    def format_name(name, ed):
        fname = name
        if ed == 3:
            parts = name.split('_')
            ln = len(parts)
            fname = ' '.join([parts[ln-1]]+parts[:ln-1])

        return fname.lower()

    @staticmethod
    def load_exisiting_wiki_names(fname):
        res = []

        f = open(fname)
        for line in f:
            res.append(line.strip())

        res.sort()
        return res

    @staticmethod
    def load_existing_brit_names(fname):
        res = []
        
        f = open(fname)
        for line in f:
            res.append((line.strip().lower(), line[:-1]))

        res.sort(key=lambda x: x[0])
        return res

    
    ###real stuff
    def get_wiki_candids(self, wname):
        i1, i2 = CandidDealer.get_candids(wname, self.existing_wiki)
        return wname[i1], wname[i2]

    def get_brit_candids_from_wiki_candids(self,wcandids):
        res  = []
        for wc in wcandids:
            try:
                art = Article.get(match_master__name=wc)
                res.append(art.name)
            except:
                pass
        return res

    def get_brit_candids(self, wname, edition):
        name = format_name(wname, edition)
        existing = map(lambda x: x[0], self.exisitng_brit)
        i1, i2 = CandidDealer.get_candids(name, exisitng)
        return self.exisitng_brit[i1][1], self.exisitng_brit[i2][1]

    def get_all_candids(self, wname, edition):
        wcandids = get_wiki_candids(wname, self.existing_wiki)
        bcandids = self.get_brit_candids_from_wiki_candids(wcandids) + self.get_brit_candids(wname, edition)
        return bcandids

    def get_best_candidate(self, wname):
        wtext = wiki.getArticleText(wname)
        candids = get_all_candids(wname)
    

if __name__=='__main__':
    #get_missing_articles('top200people')
    get_edition_matches(3)
