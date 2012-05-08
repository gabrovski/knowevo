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

###candidate finding classes follow
class Dealer:
    def __init__(self, exisiting_path, edition):
        self.existing = self.load_existing_names(exisiting_path)
        self.editon = edition

    @abstractmethod
    def load_existing_names(self, fname):
        pass
    
    def format_name(self, name):
        fname = name
        if self.ed == 3:
            parts = name.split('_')
            ln = len(parts)
            fname = ' '.join([parts[ln-1]]+parts[:ln-1])

        return fname.lower()

    def get_candid_indeces(self, wname):
        wname = self.format_name(wname)
        index = bisect.bisect_left(wname, self.exisiting)
        return index, index+1

    def get_candids(self, wname):
        i1, i2 = self.get_candid_indeces(wname)
        return self.existing[i1], self.existing[i2]


###wiki only candids
class WikiDealer(Dealer):
    def __init__(self, exist_wiki_path):
        super(Dealer,self).__init__(exist_wiki_path, 'wiki')

    #Override
    def load_exisiting_names(self, fname):
        res = []

        f = open(fname)
        for line in f:
            res.append(line.strip())

        res.sort()
        return res


###candid generation
class CandidDealer(Dealer):
    def __init__(self, exist_brit_path, edition, wikidealer):
        super(Dealer,self).__init__(exist_brit_path, edition)
        self.wikidealer = wikidealer

    #Override
    def load_existing_names(self, fname):
        res = []
        
        f = open(fname)
        for line in f:
            res.append((line.strip().lower(), line[:-1]))

        res.sort(key=lambda x: x[0])

        self.existing_tuples = res
        return map(lambda x: x[0], res)

    def get_brit_candids_from_wiki_candids(self, wname):
        res  = []
        wcandids = self.wikidealer.get_candids(wname)
        for wc in wcandids:
            try:
                art = Article.get(match_master__name=wc)
                res.append(art.name)
            except:
                pass
        return res

    #Override
    def get_candids(self, wname):
        i1, i2 = super(Dealer,self).get_candid_indeces(wname)
        return self.exisitng_tuples[i1][1], self.exisitng_tuples[i2][1]

    def get_all_candids(self, wname):
        bcandids = self.get_brit_candids_from_wiki_candids(wname) + self.get_candids(wname)
        return bcandids

    def get_best_candidate(self, wname):
        wtext = wiki.getArticleText(wname)
        candids = get_all_candids(wname)
    

if __name__=='__main__':
    #get_missing_articles('top200people')
    get_edition_matches(3)
